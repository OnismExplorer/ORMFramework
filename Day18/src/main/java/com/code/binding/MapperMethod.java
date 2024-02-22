package com.code.binding;

import com.code.mapping.MappedStatement;
import com.code.mapping.SqlCommandType;
import com.code.session.Configuration;
import com.code.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;


/**
 * Mapper 映射方法
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class MapperMethod {
    private final SqlCommand command;

    private final MethodSignature methodSignature;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
        this.methodSignature = new MethodSignature(configuration, method);
    }

    /**
     * 执行方法
     *
     * @param sqlSession SQL 会话
     * @param args       参数
     * @return {@link Object}
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        switch (command.getType()) {
            case INSERT -> {
                Object param = methodSignature.convertArgsSqlCommandParameter(args);
                result = sqlSession.insert(command.getName(), param);
            }
            case DELETE -> {
                Object param = methodSignature.convertArgsSqlCommandParameter(args);
                result = sqlSession.delete(command.getName(), param);
            }
            case UPDATE -> {
                Object param = methodSignature.convertArgsSqlCommandParameter(args);
                result = sqlSession.update(command.getName(), param);
            }
            case SELECT -> {
                Object param = methodSignature.convertArgsSqlCommandParameter(args);
                if (methodSignature.returnMany) {
                    result = sqlSession.selectList(command.getName(), param);
                } else {
                    result = sqlSession.selectOne(command.getName(), param);
                }
            }
            default -> throw new RuntimeException("未知执行方法：" + command.getName());
        }
        return result;
    }

    /**
     * sql 命令
     *
     * @author HeXin
     * @date 2024/01/25
     */
    public static class SqlCommand {
        private final SqlCommandType type;
        private final String name;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement statement = configuration.getMappedStatement(statementName);
            name = statement.getId();
            type = statement.getSqlCommandType();
        }

        public SqlCommandType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 方法签名
     *
     * @author HeXin
     * @date 2024/02/11
     */
    public static class MethodSignature {

        /**
         * 是否返回多个结果
         */
        private final boolean returnMany;

        private final Class<?> returnType;
        private final SortedMap<Integer, String> parameters;

        public MethodSignature(Configuration configuration, Method method) {
            /*
              返回类型
             */
            returnType = method.getReturnType();
            this.returnMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
            this.parameters = Collections.unmodifiableSortedMap(getParameters(method));
        }

        /**
         * 获取参数参数
         *
         * @param method 方法
         * @return {@link SortedMap}<{@link Integer},{@link String}>
         */
        private SortedMap<Integer, String> getParameters(Method method) {
            // 使用 TreeMap 保证按参数的先后顺序
            final SortedMap<Integer, String> parameters = new TreeMap<>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                String parameterName = String.valueOf(parameters.size());
                parameters.put(i, parameterName);
            }
            return parameters;
        }

        /**
         * 是否返回多个结果
         *
         * @return boolean
         */
        public boolean isReturnMany() {
            return returnMany;
        }

        /**
         * 转换参数列表为 SQL命令参数
         *
         * @param args arg游戏
         * @return {@link Object}
         */
        public Object convertArgsSqlCommandParameter(Object[] args) {
            final int parameterCount = parameters.size();
            if (args == null || parameterCount == 0) {
                // 没有参数
                return null;
            } else if (parameterCount == 1) {
                // 有一个参数
                return args[parameters.keySet().iterator().next()];
            } else {
                // 返回一个 ParameterMap，修改参数名，参数名即其位置
                final Map<String, Object> parameterMap = new ParameterMap<>();
                int index = 0;
                for (Map.Entry<Integer, String> entry : parameters.entrySet()) {
                    // 先添加一个#{0}，#{1}...参数
                    parameterMap.put(entry.getValue(), args[entry.getKey()]);
                    final String genericParameterName = "param" + (index + 1);
                    // 再添加一个#{param0}，#{param1}...参数
                    parameterMap.computeIfAbsent(genericParameterName, key -> args[entry.getKey()]);
                    index++;
                }
                return parameterMap;
            }
        }
    }

    public static class ParameterMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = 2883920188826439065L;

        /**
         * 获取
         *
         * @param key 关键
         * @return {@link V}
         */
        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new RuntimeException("未找到参数 '" + key + "'。可用参数包括 " + keySet());
            }
            return super.get(key);
        }
    }
}
