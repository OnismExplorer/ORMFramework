package com.code.reflection;

import com.code.reflection.invoker.GetFieldInvoker;
import com.code.reflection.invoker.Invoker;
import com.code.reflection.invoker.MethodInvoker;
import com.code.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * 元类
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class MetaClass {

    private Reflector reflector;

    public MetaClass(Class<?> type) {
        this.reflector = Reflector.forClass(type);
    }

    /**
     * 对于类
     *
     * @param type 类型
     * @return {@link MetaClass}
     */
    public static MetaClass forClass(Class<?> type) {
        return new MetaClass(type);
    }

    /**
     * 是否启用了类缓存
     *
     * @return boolean
     */
    public static boolean isClassCacheEnabled() {
        return Reflector.isClassCacheEnabled();
    }

    /**
     * 设置启用类缓存
     *
     * @param classCacheEnabled 是否启用类缓存
     */
    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.setClassCacheEnabled(classCacheEnabled);
    }

    /**
     * 属性元类
     *
     * @param name 名字
     * @return {@link MetaClass}
     */
    public MetaClass metaClassForProperty(String name) {
        Class<?> properType = reflector.getGetterType(name);
        return MetaClass.forClass(properType);
    }

    /**
     * 查找属性
     *
     * @param name 名字
     * @return {@link String}
     */
    public String findProperty(String name) {
        StringBuilder propertyName = buildProperty(name,new StringBuilder());
        return propertyName.length() > 0 ? propertyName.toString() : null;
    }

    /**
     * 查找属性
     *
     * @param name                名字
     * @param useCamelCaseMapping 是否使用驼峰大小写映射
     * @return {@link String}
     */
    public String findProperty(String name,boolean useCamelCaseMapping) {
        if(useCamelCaseMapping) {
            name = name.replace("_","");
        }
        return findProperty(name);
    }

    /**
     * 获取 GETTER 名称
     *
     * @return {@link String[]}
     */
    public String[] getGetterNames() {
        return reflector.getGetablePropertyNames();
    }

    /**
     * 获取 setter 名称
     *
     * @return {@link String[]}
     */
    public String[] getSetterNames() {
        return reflector.getSetablePropertyNames();
    }

    /**
     * 获取 setter 类型
     *
     * @param name 名字
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> getSetterType(String name) {
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if(propertyTokenizer.hasNext()) {
            MetaClass metaProperty = metaClassForProperty(propertyTokenizer.getName());
            return metaProperty.getSetterType(propertyTokenizer.getChildren());
        }
        return reflector.getSetterType(propertyTokenizer.getName());
    }

    /**
     * 获取 getter 类型
     *
     * @param name 名字
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> getGetterType(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if(tokenizer.hasNext()) {
            MetaClass metaClass = metaClassForProperty(tokenizer);
            return metaClass.getGetterType(tokenizer.getChildren());
        }
        // 解析 Collection 对象中的类型
        return getGetterType(tokenizer);
    }

    /**
     * 属性元类
     *
     * @param tokenizer 分词器
     * @return {@link MetaClass}
     */
    private MetaClass metaClassForProperty(PropertyTokenizer tokenizer) {
        Class<?> propertyType = getGetterType(tokenizer);
        return MetaClass.forClass(propertyType);
    }

    /**
     * 获取属性的getter方法的返回类型。
     *
     * @param tokenizer 属性解析器，用于解析属性的名称和索引
     * @return 属性的getter方法的返回类型
     */
    private Class<?> getGetterType(PropertyTokenizer tokenizer) {
        // 使用反射器获取属性的getter方法的返回类型
        Class<?> type = reflector.getGetterType(tokenizer.getName());

        // 如果属性带有索引且返回类型是集合，则获取泛型返回类型
        if (tokenizer.getIndex() != null && Collection.class.isAssignableFrom(type)) {
            // 获取泛型返回类型
            Type returnType = getGenericGetterType(tokenizer.getName());

            // 如果泛型返回类型是ParameterizedType
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();

                // 如果存在实际类型参数且长度为1
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnType = actualTypeArguments[0];

                    // 如果实际类型是Class类型，则作为返回类型
                    if (returnType instanceof Class) {
                        type = (Class<?>) returnType;
                    } else if (returnType instanceof ParameterizedType) {
                        // 如果实际类型是ParameterizedType，则获取其原始类型作为返回类型
                        type = (Class<?>) ((ParameterizedType) returnType).getRawType();
                    }
                }
            }
        }

        // 返回属性的getter方法的返回类型
        return type;
    }

    /**
     * 获取属性的getter方法的泛型返回类型。
     *
     * @param propertyName 属性的名称
     * @return 属性的getter方法的泛型返回类型
     */
    private Type getGenericGetterType(String propertyName) {
        try {
            // 使用反射器获取属性的getter方法调用器（Invoker）
            Invoker invoker = reflector.getGetInvoker(propertyName);

            // 如果调用器是MethodInvoker类型
            if (invoker instanceof MethodInvoker) {
                // 获取MethodInvoker类的私有字段"method"
                Field _method = MethodInvoker.class.getDeclaredField("method");
                _method.setAccessible(true);

                // 获取MethodInvoker的底层Method对象
                Method method = (Method) _method.get(invoker);

                // 返回Method的泛型返回类型
                return method.getGenericReturnType();
            } else if (invoker instanceof GetFieldInvoker) {
                // 如果调用器是GetFieldInvoker类型
                // 获取GetFieldInvoker类的私有字段"field"
                Field _field = GetFieldInvoker.class.getDeclaredField("field");
                _field.setAccessible(true);

                // 获取GetFieldInvoker的底层Field对象
                Field field = (Field) _field.get(invoker);

                // 返回Field的泛型类型
                return field.getGenericType();
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 处理异常，例如输出错误信息
            System.err.println(e.getMessage());
        }

        // 默认返回null
        return null;
    }

    /**
     * 是否含有 setter
     *
     * @param name 名字
     * @return boolean
     */
    public boolean hasSetter(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if(tokenizer.hasNext()) {
            if(reflector.hasSetter(tokenizer.getName())) {
                MetaClass metaClass = metaClassForProperty(tokenizer.getName());
                return metaClass.hasSetter(tokenizer.getChildren());
            } else {
                return false;
            }
        }
        return reflector.hasSetter(tokenizer.getName());
    }

    /**
     * 获取 Get 调用程序
     *
     * @param name 名字
     * @return {@link Invoker}
     */
    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }

    /**
     * 获取 Set 调用程序
     *
     * @param name 名字
     * @return {@link Invoker}
     */
    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }

    /**
     * 构建属性表达式，将属性名转换为对应的数据库字段名。
     *
     * @param name    属性名
     * @param builder StringBuilder对象，用于构建属性表达式
     * @return 构建好的属性表达式
     */
    private StringBuilder buildProperty(String name, StringBuilder builder) {
        // 使用属性解析器解析属性名
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);

        // 如果属性有下级属性
        if (tokenizer.hasNext()) {
            // 查找属性名，将其添加到StringBuilder中，并追加"."符号
            String propertyName = reflector.findPropertyName(tokenizer.getName());
            if (propertyName != null) {
                builder.append(propertyName);
                builder.append(".");

                // 获取下级属性的MetaClass，递归构建属性表达式
                MetaClass metaClass = metaClassForProperty(propertyName);
                metaClass.buildProperty(tokenizer.getChildren(), builder);
            }
        } else {
            // 如果属性没有下级属性，查找属性名并添加到StringBuilder中
            String propertyName = reflector.findPropertyName(name);
            if (propertyName != null) {
                builder.append(propertyName);
            }
        }

        // 返回构建好的属性表达式
        return builder;
    }

    /**
     * 是否具有默认构造函数
     *
     * @return boolean
     */
    public boolean hasDefaultConstructor() {
        return reflector.hasDefaultConstructor();
    }

    public boolean hasGetter(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if (tokenizer.hasNext()) {
            if (reflector.hasGetter(tokenizer.getName())) {
                MetaClass metaProp = metaClassForProperty(tokenizer);
                return metaProp.hasGetter(tokenizer.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasGetter(tokenizer.getName());
        }

    }
}
