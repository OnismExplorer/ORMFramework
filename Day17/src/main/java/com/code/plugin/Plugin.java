package com.code.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 插件(代理模式)
 *
 * @author HeXin
 * @date 2024/02/21
 */
public class Plugin implements InvocationHandler {

    private Object target;

    private Interceptor interceptor;

    /**
     * 方法签名集合
     */
    private Map<Class<?>, Set<Method>> signatureMap;

    public Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    /**
     * 用代理将自定义插件行为包装到目标方法( Plugin.invoke )中过滤调用
     *
     * @param target      目标
     * @param interceptor 拦截器
     * @return {@link Object}
     */
    public static Object wrap(Object target,Interceptor interceptor) {
        // 获取签名 Map
        Map<Class<?>,Set<Method>> signatureMap = getSignatureMap(interceptor);
        // 获取需要改变行为的类(ParameterHandler | ResultSetHandler |StatementHandler | Executor)
        Class<?> type = target.getClass();
        // 获取接口
        Class<?>[] interfaces = getAllInterfaces(type,signatureMap);
        // 创建代理
        if(interfaces.length > 0) {
            return Proxy.newProxyInstance(type.getClassLoader(),interfaces,new Plugin(target,interceptor,signatureMap));
        }
        return target;
    }

    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        // 获取 Intercepts 注解
        Intercepts annotation = interceptor.getClass().getAnnotation(Intercepts.class);
        // 获取不到则抛出异常
        if(annotation == null) {
            throw new RuntimeException("拦截器 " + interceptor.getClass().getName() + " 未包含 @Intercepts 注解");
        }
        // 获取注解 Signature 类型的value
        Signature[] signatures = annotation.value();
        // 每个 class 类可能有多个 Method 需要拦截
        HashMap<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        for (Signature signature : signatures) {
            Set<Method> methods = signatureMap.computeIfAbsent(signature.type(), key -> new HashSet<>());
            try {
                // 获取方法
                Method method = signature.type().getMethod(signature.method(), signature.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("在类型 " + signature.type() + " 中未找到名为 " + signature.method() + " 的方法。原因：" + e, e);
            }
        }
        return signatureMap;
    }

    public static Class<?>[] getAllInterfaces(Class<?> type,Map<Class<?>,Set<Method>> signatureMap) {
        HashSet<Class<?>> interfaces = new HashSet<>();
        while(type != null) {
            for (Class<?> clazz : type.getInterfaces()) {
                // 拦截 ParameterHandler|ResultSetHandler|StatementHandler|Executor
                if(signatureMap.containsKey(clazz)) {
                    interfaces.add(clazz);
                }
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[0]);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取声明的方法列表
        Set<Method> methods = signatureMap.get(method.getDeclaringClass());
        // 过滤需要拦截的方法
        if(methods != null && methods.contains(method)) {
            // 插入自己的反射逻辑
            return interceptor.intercept(new Invocation(target,method,args));
        }
        return method.invoke(target,args);
    }
}
