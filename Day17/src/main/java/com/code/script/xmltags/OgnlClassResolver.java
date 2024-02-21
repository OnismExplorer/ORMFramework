package com.code.script.xmltags;

import com.code.io.Resources;
import ognl.ClassResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Ognl类解析器
 *
 * @author HeXin
 * @date 2024/02/20
 */
public class OgnlClassResolver implements ClassResolver {

    private Map<String,Class<?>> classes = new HashMap<>(101);

    /**
     * 通过名称获取类
     *
     * @param className 类名
     * @param map 地图
     * @return {@link Class}
     * @throws ClassNotFoundException 类未发现异常
     */
    @Override
    public Class classForName(String className, Map map) throws ClassNotFoundException {
        Class<?> result = null;
        if((result = classes.get(className)) == null) {
            try {
                result = Resources.classForName(className);
            } catch (ClassNotFoundException e) {
                if(className.indexOf('.') == -1) {
                    result = Resources.classForName("java.lang." + className);
                    classes.put("java.lang." + className,result);
                }
            }
            classes.put(className,result);
        }
        return result;
    }
}
