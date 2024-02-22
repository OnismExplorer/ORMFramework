package com.code.reflection;

import com.code.reflection.invoker.Invoker;
import com.code.reflection.invoker.MethodInvoker;
import com.code.reflection.invoker.SetFieldInvoker;
import com.code.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.getSecurityManager;

/**
 * 反射器(属性get/set的映射器)
 *
 * @author HeXin
 * @date 2024/02/01
 */
public class Reflector {
    /**
     * 空字符串数组
     */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    /**
     * 反射器 Map 集合
     */
    private static final Map<Class<?>, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();
    /**
     * 是否启用类缓存
     */
    private static  boolean classCacheEnabled = true;
    /**
     * 不区分大小写属性映射
     */
    private final Map<String, String> caseInsensitivePropertyMap = new HashMap<>();
    /**
     * 类型
     */
    private final Class<?> type;
    /**
     * Set 方法列表
     */
    private final Map<String, Invoker> setMethods = new HashMap<>();
    /**
     * get 方法列表
     */
    private final Map<String, Invoker> getMehods = new HashMap<>();
    /**
     * set 类型列表
     */
    private final Map<String, Class<?>> setTypes = new HashMap<>();
    /**
     * get 类型列表
     */
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    /**
     * get 属性名称
     */
    private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
    /**
     * set 属性名称
     */
    private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;
    /**
     * 默认构造函数
     */
    private Constructor<?> defaultConstructor;

    public Reflector(Class<?> clazz) {
        this.type = clazz;
        // 添加构造函数
        addDefaultConstructor(clazz);
        // 添加 getter
        addGetMethods(clazz);
        // 添加 setter
        addSetMethods(clazz);
        // 加入字段
        addFields(clazz);
        readablePropertyNames = getMehods.keySet().toArray(new String[0]);
        writeablePropertyNames = setMethods.keySet().toArray(new String[0]);
        for (String propertyName : readablePropertyNames) {
            // 映射 get 方法
            caseInsensitivePropertyMap.put(propertyName.toUpperCase(Locale.ENGLISH), propertyName);
        }
        for (String propertyName : writeablePropertyNames) {
            // 映射 set 方法
            caseInsensitivePropertyMap.put(propertyName.toUpperCase(Locale.ENGLISH), propertyName);
        }
    }

    /**
     * 添加默认构造函数
     *
     * @param clazz 类
     */
    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                if (canAccessPrivateMethods()) {
                    try {
                        constructor.setAccessible(true);
                    } catch (Exception ignore) {
                        // ignore
                    }
                }

                // 判断 constructor 是否可访问
                if (constructor.isAccessible()) {
                    this.defaultConstructor = constructor;
                }
            }
        }
    }

    /**
     * 添加 GET 方法
     *
     * @param clazz 克拉兹
     */
    private void addGetMethods(Class<?> clazz) {

        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get") && name.length() > 3 && method.getParameterTypes().length == 0) {
                name = PropertyNamer.methodToProperty(name);
                addMethodConflict(conflictingGetters, name, method);
            } else if (name.startsWith("is") && name.length() == 0 && method.getParameterTypes().length == 0) {
                name = PropertyNamer.methodToProperty(name);
                addMethodConflict(conflictingGetters, name, method);
            }
        }
        resolveGetterConflicts(conflictingGetters);
    }

    /**
     * 添加 Set 方法
     *
     * @param clazz 类
     */
    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && name.length() > 3 && method.getParameterTypes().length == 1) {
                name = PropertyNamer.methodToProperty(name);
                addMethodConflict(conflictingSetters, name, method);
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    /**
     * 解决 setter 冲突
     *
     * @param conflictingSetters setter 冲突
     */
    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingSetters.entrySet()) {
            String propertyName = entry.getKey();
            // 获取属性的所有冲突setter方法
            List<Method> setters = conflictingSetters.get(propertyName);
            // 获取第一个setter方法
            Method firstMethod = setters.get(0);

            // 如果只有一个setter方法，直接添加
            if (setters.size() == 1) {
                addSetMethod(propertyName, firstMethod);
            } else {
                // 获取属性的预期类型
                Class<?> expectedType = getTypes.get(propertyName);

                // 如果预期类型为null，则抛出异常，表示存在非法的重载setter方法
                if (expectedType == null) {
                    throw new RuntimeException("在类 " + firstMethod.getDeclaringClass() + " 中，属性 " + propertyName +
                            " 存在非法的重载 setter 方法，其类型模糊不清。这违反了JavaBeans规范，可能导致不可预测的结果。");
                } else {
                    // 遍历冲突setter方法，选择符合预期类型的setter方法
                    Iterator<Method> methods = setters.iterator();
                    Method setter = null;
                    while (methods.hasNext()) {
                        Method method = methods.next();
                        if (method.getParameterTypes().length == 1 && expectedType.equals(method.getParameterTypes()[0])) {
                            setter = method;
                            break;
                        }
                    }

                    // 如果未找到符合预期类型的setter方法，则抛出异常
                    if (setter == null) {
                        throw new RuntimeException("在类 " + firstMethod.getDeclaringClass() + " 中，属性 " + propertyName +
                                " 存在非法的重载 setter 方法，其类型模糊不清。这违反了JavaBeans规范，可能导致不可预测的结果。");
                    }

                    // 添加选择的setter方法
                    addSetMethod(propertyName, setter);
                }
            }
        }
    }

    /**
     * 添加 Set 方法
     *
     * @param name   名字
     * @param method
     */
    private void addSetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            setMethods.put(name, new MethodInvoker(method));
            setTypes.put(name, method.getParameterTypes()[0]);
        }
    }

    /**
     * 解决JavaBeans规范中属性的重载getter方法冲突的方法。
     *
     * @param conflictingGetters 包含冲突getter方法的映射，键为属性名，值为对应属性的getter方法列表
     */
    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
            // 获取属性名和所有冲突getter方法
            String propertyName = entry.getKey();
            List<Method> getters = entry.getValue();
            Iterator<Method> iterator = getters.iterator();
            Method firstMethod = iterator.next();

            // 如果只有一个getter方法，直接添加
            if (getters.size() == 1) {
                addGetMethod(propertyName, firstMethod);
            } else {
                // 初始化getter和getter类型
                Method getter = firstMethod;
                Class<?> getterType = firstMethod.getReturnType();

                // 遍历冲突getter方法，选择最合适的getter方法
                while (iterator.hasNext()) {
                    Method method = iterator.next();
                    Class<?> methodType = method.getReturnType();

                    // 检查类型是否相等，如果相等，抛出异常
                    if (methodType.equals(getterType)) {
                        throw new RuntimeException("在类 " + firstMethod.getDeclaringClass() + " 中，属性 " + propertyName +
                                " 存在非法的重载 setter 方法，其类型模糊不清。这违反了JavaBeans规范，可能导致不可预测的结果。");
                    } else if (methodType.isAssignableFrom(getterType)) {
                        // 如果methodType是getterType的子类，不做任何处理
                    } else if (getterType.isAssignableFrom(methodType)) {
                        // 如果getterType是methodType的子类，则选择更具体的getter方法
                        getter = method;
                        getterType = methodType;
                    } else {
                        // 如果两者没有继承关系，抛出异常
                        throw new RuntimeException("在类 " + firstMethod.getDeclaringClass() + " 中，属性 " + propertyName +
                                " 存在非法的重载 setter 方法，其类型模糊不清。这违反了JavaBeans规范，可能导致不可预测的结果。");
                    }
                }

                // 添加选择的getter方法
                addGetMethod(propertyName, getter);
            }
        }
    }

    /**
     * 添加 Get 方法
     *
     * @param propertyName 属性名称
     * @param method       吸气剂
     */
    private void addGetMethod(String propertyName, Method method) {
        if (isValidPropertyName(propertyName)) {
            getMehods.put(propertyName, new MethodInvoker(method));
            getTypes.put(propertyName, method.getReturnType());
        }

    }

    /**
     * 添加字段
     *
     * @param clazz
     */
    private void addFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (canAccessPrivateMethods()) {
                try {
                    field.setAccessible(true);
                } catch (Exception ignore) {
                    // ignore
                }
            }
            if (field.isAccessible()) {
                if (!setMethods.containsKey(field.getName())) {
                    int modifiers = field.getModifiers();
                    if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
                        addSetField(field);
                    }
                }
                if (!getMehods.containsKey(field.getName())) {
                    addGetField(field);
                }
            }
        }
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    /**
     * 添加 set 字段
     *
     * @param field 字段
     */
    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            setTypes.put(field.getName(), field.getType());
        }
    }

    /**
     * 增加 get 字段
     *
     * @param field 田
     */
    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            getMehods.put(field.getName(), new SetFieldInvoker(field));
            getTypes.put(field.getName(), field.getType());
        }
    }

    /**
     * 是否为有效属性名称
     *
     * @param name 名字
     * @return boolean
     */
    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    /**
     * 添加方法冲突
     *
     * @param name               名字
     * @param conflictingMethods 冲突方法集合
     * @param method             方法
     */
    private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
        List<Method> list = conflictingMethods.computeIfAbsent(name, a -> new ArrayList<>());
        list.add(method);
    }

    /**
     * Get 类方法
     *
     * @param clazz 类
     * @return {@link Method[]}
     */
    private Method[] getClassMethods(Class<?> clazz) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }

            currentClass = currentClass.getSuperclass();
        }
        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[0]);
    }

    /**
     * 向唯一方法集合中添加非桥接方法。
     *
     * @param uniqueMethods 包含唯一方法的映射，键为方法签名，值为对应方法
     * @param methods       要添加的方法数组
     */
    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            // 检查当前方法是否为桥接方法
            if (!currentMethod.isBridge()) {
                // 获取方法签名
                String signature = getSignature(currentMethod);

                // 如果唯一方法集合中不包含当前方法的签名，则添加
                if (!uniqueMethods.containsKey(signature)) {
                    // 如果可以访问私有方法，尝试设置方法为可访问
                    if (canAccessPrivateMethods()) {
                        try {
                            currentMethod.setAccessible(true);
                        } catch (Exception ignore) {
                            // 忽略异常
                        }
                    }

                    // 将方法添加到唯一方法集合中
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }



    /**
     * 获取方法的签名，包括返回类型、方法名和参数类型。
     *
     * @param method 要获取签名的方法
     * @return 方法的签名字符串
     */
    private String getSignature(Method method) {
        // 使用StringBuilder构建方法签名
        StringBuilder builder = new StringBuilder();

        // 获取方法的返回类型并追加到签名中
        Class<?> returnType = method.getReturnType();
        builder.append(returnType.getName()).append("#");

        // 获取方法名并追加到签名中
        builder.append(method.getName());

        // 获取方法的参数类型数组
        Class<?>[] parameters = method.getParameterTypes();

        // 遍历参数类型数组，追加到签名中
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                builder.append(":");
            } else {
                builder.append(",");
            }
            builder.append(parameters[i].getName());
        }

        // 返回构建的方法签名字符串
        return builder.toString();
    }


    /**
     * 能否访问私有方法
     *
     * @return boolean
     */
    private static boolean canAccessPrivateMethods() {
        try {
            SecurityManager securityManager = getSecurityManager();
            if(securityManager != null) {
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    public Class<?> getType() {
        return type;
    }

    /**
     * 获取默认构造器
     *
     * @return {@link Constructor}<{@link ?}>
     */
    public Constructor<?> getDefaultConstructor(){
        if(defaultConstructor != null) {
            return defaultConstructor;
        }
        throw new RuntimeException(type+" 类没有默认构造器！");
    }

    /**
     * 是否具有默认构造函数
     *
     * @return boolean
     */
    public boolean hasDefaultConstructor(){
        return defaultConstructor != null;
    }

    /**
     * 获取 setter 类型
     *
     * @param propertyName 属性名称
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> getSetterType(String propertyName) {
        Class<?> clazz = setTypes.get(propertyName);
        if(clazz == null) {
            throw new RuntimeException("在 '" + type + "' 类中没有名为 '" + propertyName + "' 的属性的setter方法。");
        }
        return clazz;
    }

    /**
     * 获取 getter 类型
     *
     * @param propertyName 属性名称
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> getGetterType(String propertyName) {
        Class<?> clazz = getTypes.get(propertyName);
        if(clazz == null) {
            throw new RuntimeException("在 '" + type + "' 类中没有名为 '" + propertyName + "' 的属性的getter方法。");
        }
        return clazz;
    }

    /**
     * 获取 Get 调用器
     *
     * @param propertyName 属性名称
     * @return {@link Invoker}
     */
    public Invoker getGetInvoker(String propertyName) {
        Invoker method = getMehods.get(propertyName);
        if(method == null) {
            throw new RuntimeException("在 '" + type + "' 类中没有名为 '" + propertyName + "' 的属性的getter方法。");
        }
        return method;
    }

    /**
     * 获取 Set 调用程序
     *
     * @param propertyName 属性名称
     * @return {@link Invoker}
     */
    public Invoker getSetInvoker(String propertyName){
        Invoker invoker = setMethods.get(propertyName);
        if(invoker == null) {
            throw new RuntimeException("在 '" + type + "' 类中没有名为 '" + propertyName + "' 的属性的getter方法。");
        }
        return invoker;
    }

    /**
     * 获取可获取属性名称
     *
     * @return {@link String[]}
     */
    public String[] getGetablePropertyNames(){
        return readablePropertyNames;
    }

    /**
     * 获取可设置属性名称
     *
     * @return {@link String[]}
     */
    public String[] getSetablePropertyNames(){
        return writeablePropertyNames;
    }

    /**
     * 是否有 setter
     *
     * @param propertyName 属性名称
     * @return boolean
     */
    public boolean hasSetter(String propertyName) {
        return setMethods.containsKey(propertyName);
    }

    /**
     * 是否有 getter
     *
     * @param propertyName 属性名称
     * @return boolean
     */
    public boolean hasGetter(String propertyName) {
        return getMehods.containsKey(propertyName);
    }

    /**
     * 查找属性名称
     *
     * @param name 名字
     * @return {@link String}
     */
    public String findPropertyName(String name) {
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    /**
     * 对于类
     *
     * @param clazz 类
     * @return {@link Reflector}
     */
    public static Reflector forClass(Class<?> clazz){
        if(classCacheEnabled) {
            // 将该类的信息(字段、构造函数、getter、setter)放入缓存提高速度(假设类是不变的)
            Reflector cache = REFLECTOR_MAP.get(clazz);
            if(cache == null) {
                cache = new Reflector(clazz);
                REFLECTOR_MAP.put(clazz,cache);
            }
            return cache;
        }
        return new Reflector(clazz);
    }

    /**
     * 设置启用类缓存
     *
     * @param classCacheEnabled 已启用类缓存
     */
    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.classCacheEnabled = classCacheEnabled;
    }

    public static boolean isClassCacheEnabled(){
        return classCacheEnabled;
    }
}
