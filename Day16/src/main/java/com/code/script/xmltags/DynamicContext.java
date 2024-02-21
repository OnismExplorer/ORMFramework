package com.code.script.xmltags;

import com.code.reflection.MetaObject;
import com.code.session.Configuration;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态上下文
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class DynamicContext {

    static {
        // 定义属性->getter方法映射，ContextMap到ContextAccessor的映射，注册到ognl运行时
        // 参考http://commons.apache.org/proper/commons-ognl/developer-guide.html
        OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
        // 将传入的参数对象统一封装为ContextMap对象（继承了HashMap对象），
        // 然后Ognl运行时环境在动态计算sql语句时，
        // 会按照ContextAccessor中描述的Map接口的方式来访问和读取ContextMap对象，获取计算过程中需要的参数。
        // ContextMap对象内部可能封装了一个普通的POJO对象，也可以是直接传递的Map对象，当然从外部是看不出来的，因为都是使用Map的接口来读取数据。
    }

    /**
     * 背景图
     *
     * @author HeXin
     * @date 2024/02/07
     */
    static class ContextMap extends HashMap<String, Object> {
        @Serial
        private static final long serialVersionUID = 992771992166151582L;

        /**
         * 参数元对象
         */
        private final MetaObject parameterMetaObject;
        public ContextMap(MetaObject parameterMetaObject) {
            this.parameterMetaObject = parameterMetaObject;
        }

        @Override
        public Object get(Object key) {
            String strKey = (String) key;
            // 先去map里找
            if (super.containsKey(strKey)) {
                return super.get(strKey);
            }

            // 如果没找到，再用ognl表达式去取值
            if (parameterMetaObject != null) {
                return parameterMetaObject.getValue(strKey);
            }

            return null;
        }
    }

    static class ContextAccessor implements PropertyAccessor {

        @Override
        @SuppressWarnings("all")
        /**
         * 获取属性值的方法，实现了 OgnlPropertyAccessor 接口中的 getProperty 方法。
         *
         * @param context OGNL 运行时的上下文
         * @param target  目标对象，这里期望是一个 Map 类型的对象
         * @param name    属性名
         * @return 属性值，如果在目标 Map 中找到属性值，则返回；如果在参数对象 Map 中找到属性值，则返回；否则返回 null。
         * @throws OgnlException OGNL 表达式异常
         */
        public Object getProperty(Map context, Object target, Object name) throws OgnlException {
            Map map = (Map) target;

            // 在目标 Map 中查找属性值
            Object result = map.get(name);
            if (result != null) {
                return result;
            }

            // 从参数对象中获取属性值
            Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
            if (parameterObject instanceof Map) {
                return ((Map) parameterObject).get(name);
            }

            // 如果都没有找到，返回 null
            return null;
        }


        @Override
        @SuppressWarnings("unchecked")
        public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
            Map<Object , Object> map = (Map<Object, Object>) target;
            map.put(name,value);
        }

        @Override
        public String getSourceAccessor(OgnlContext ognlContext, Object o, Object o1) {
            return null;
        }

        @Override
        public String getSourceSetter(OgnlContext ognlContext, Object o, Object o1) {
            return null;
        }
    }

    public static final String PARAMETER_OBJECT_KEY = "_parameter";

    public static final String DATABASE_ID_KEY = "_databaseId";
    private final ContextMap bindings;
    private final StringBuilder builder = new StringBuilder();

    /**
     * 独特数字
     */
    private int uniqueNumber = 0;

    public DynamicContext(Configuration configuration,Object parameterObject) {
        if(parameterObject != null && !(parameterObject instanceof  Map)) {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            bindings = new ContextMap(metaObject);
        } else {
            bindings = new ContextMap(null);
        }
        bindings.put(PARAMETER_OBJECT_KEY,parameterObject);
        bindings.put(DATABASE_ID_KEY,configuration.getDataBaseId());
    }

    public Map<String,Object> getBindings() {
        return bindings;
    }

    public void bind(String name,Object value) {
        bindings.put(name,value);
   }

    /**
     * 添加sql
     *
     * @param sql sql
     */
    public void appendSql(String sql) {
        builder.append(sql);
        builder.append(" ");
    }

    /**
     * 获得sql
     *
     * @return {@link String}
     */
    public String getSql() {
        return builder.toString().trim();
    }

    /**
     * 获取唯一编号
     *
     * @return int
     */
    public int getUniqueNumber() {
        return uniqueNumber++;
    }
}
