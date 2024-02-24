package com.code.builder.annotation;

import com.code.annotations.*;
import com.code.binding.MapperMethod;
import com.code.builder.MapperBuilderAssistant;
import com.code.executor.keygen.Jdbc3KeyGenerator;
import com.code.executor.keygen.KeyGenerator;
import com.code.executor.keygen.NoKeyGenerator;
import com.code.mapping.SqlCommandType;
import com.code.mapping.SqlSource;
import com.code.script.LanguageDriver;
import com.code.session.Configuration;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;
import com.mysql.fabric.xmlrpc.base.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 注解配置构建器
 *
 * @author HeXin
 * @date 2024/02/14
 */
public class MapperAnnotationBuilder {

    /**
     * SQL注释类型
     */
    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<>();

    private Configuration configuration;

    private MapperBuilderAssistant assistant;

    private Class<?> type;

    public MapperAnnotationBuilder(Configuration configuration,Class<?> type) {
        String resource = type.getName().replace(".","/") + ".java";
        this.assistant = new MapperBuilderAssistant(configuration,resource);
        this.configuration = configuration;
        this.type = type;

        // 添加 CRUD 注解
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Delete.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Select.class);
    }

    /**
     * 解析注解中 SQL 语句
     */
    public void parse() {
        String resource = type.toString();
        if(!configuration.isResourceLoaded(resource)) {
            assistant.setCurrentNameSpace(type.getName());

            Method[] methods = type.getMethods();
            for (Method method : methods) {
                // 如果不是桥接方法则解析该方法
                if(!method.isBridge()) {
                    // 解析语句
                    parseStatement(method);
                }
            }
        }
    }

    /**
     * 解析给定方法上的注解，生成对应的 MappedStatement，并将其添加到 MyBatis 的 Configuration 中。
     *
     * @param method 要解析的方法
     */
    private void parseStatement(Method method) {
        // 获取方法的参数类型
        Class<?> parameterTypeClass = getParameterType(method);

        // 获取语言驱动器
        LanguageDriver driver = getLanguageDriver(method);

        // 从方法的注解中获取 SqlSource
        SqlSource sqlSource = getSqlSourceFromAnnotation(method, parameterTypeClass, driver);

        if (sqlSource != null) {
            // 构建映射语句的唯一标识符
            final String mappedStatementId = type.getName() + "." + method.getName();

            // 获取 SQL 命令类型
            SqlCommandType sqlCommandType = getSqlCommandType(method);

            KeyGenerator keyGenerator;
            String keyProperty = "id";
            if(SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
                keyGenerator = configuration.isUseGeneratedKeys() ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            } else {
                keyGenerator = new NoKeyGenerator();
            }

            // 判断是否为查询操作
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

            String resultMapId = null;
            if (isSelect) {
                // 解析 ResultMap 注解，获取结果映射标识符
                resultMapId = parseResultMap(method);
            }

            // 将解析得到的信息添加到 MappedStatement 中
            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    sqlCommandType,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    false,
                    false,
                    keyGenerator,
                    keyProperty,
                    driver
            );
        }
    }

    /**
     * 获取方法的返回类型。
     *
     * @param method 要获取返回类型的方法
     * @return 方法的返回类型
     */
    private Class<?> getReturnType(Method method) {
        // 获取方法的原始返回类型
        Class<?> returnType = method.getReturnType();

        // 如果返回类型是集合类型（Collection），则进一步获取泛型参数的实际类型
        if (Collection.class.isAssignableFrom(returnType)) {
            // 获取方法的泛型返回类型
            Type returnTypeParameter = method.getGenericReturnType();

            // 如果是参数化类型（ParameterizedType），则获取其泛型参数
            if (returnTypeParameter instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();

                // 如果有一个泛型参数，则更新返回类型为该参数的实际类型
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnTypeParameter = actualTypeArguments[0];

                    // 如果泛型参数是 Class 类型，则直接使用
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class<?>) returnTypeParameter;
                    }
                    // 如果泛型参数是 ParameterizedType 类型，则获取其原始类型
                    else if (returnTypeParameter instanceof ParameterizedType) {
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                    // 如果泛型参数是 GenericArrayType 类型，则获取其组件类型并创建数组类型
                    else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }

        // 返回最终的返回类型
        return returnType;
    }


    /**
     * 解析结果映射
     *
     * @param method 方法
     * @return {@link String}
     */
    private String parseResultMap(Method method) {
        StringBuilder suffix = new StringBuilder();
        for (Class<?> parameterType : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(parameterType.getSimpleName());
        }
        if(suffix.length() < 1){
            suffix.append("-void");
        }
        String resultMapId = type.getName() + "." + method.getName() + suffix;

        Class<?> returnType = getReturnType(method);
        assistant.addResultMap(resultMapId,returnType,new ArrayList<>());
        return resultMapId;
    }

    /**
     * 获取SQL命令类型
     *
     * @param method 方法
     * @return {@link SqlCommandType}
     */
    private SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = getSqlAnnotationType(method);
        if(type == null) {
            return SqlCommandType.UNKNOWN;
        }
        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    /**
     * 从方法的注解中获取 SqlSource 对象。该方法主要用于处理方法上的 SQL 相关的注解，构建对应的 SqlSource 对象。
     *
     * @param method         要处理的方法
     * @param parameterType  方法的参数类型
     * @param languageDriver 语言驱动器，用于解析 SQL
     * @return SqlSource 对象，表示从注解中获取的 SQL
     * @throws RuntimeException 如果在处理注解过程中发生异常
     */
    private SqlSource getSqlSourceFromAnnotation(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
        try {
            // 获取与 SQL 相关的注解类型
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            if (sqlAnnotationType != null) {
                // 获取方法上的 SQL 相关注解
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                // 获取注解中的 value 属性值，即 SQL 语句
                final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
                // 构建 SqlSource 对象
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        } catch (Exception e) {
            // 处理异常，抛出运行时异常
            throw new RuntimeException("无法在 SQL 注解上找到 `value` 方法：" + e);
        }
    }

    /**
     * 从字符串构建SQL源代码
     *
     * @param strings        字符串
     * @param parameterType  参数类型
     * @param languageDriver 语言司机
     * @return {@link SqlSource}
     */
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterType, LanguageDriver languageDriver) {
        final StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }
        return languageDriver.createSqlSource(configuration,sql.toString(),parameterType);
    }

    /**
     * 获取SQL注释类型
     *
     * @param method 方法
     * @return {@link Class}<{@link ?} {@link extends} {@link Annotation}>
     */
    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        for (Class<? extends Annotation> type : sqlAnnotationTypes) {
            Annotation annotation = method.getAnnotation(type);
            if(annotation != null) {
                return type;
            }
        }
        return null;
    }


    private LanguageDriver getLanguageDriver(Method method) {
        Class<?> driverClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        return configuration.getLanguageRegistry().getDriver(driverClass);
    }


    /**
     * 获取方法的参数类型。该方法用于判断方法的参数类型，特别处理了 RowBounds 和 ResultHandler 类型。
     * 如果方法只有一个非 RowBounds 和 ResultHandler 的参数，则返回该参数的类型；
     * 如果存在多个参数或者有 RowBounds 或 ResultHandler 类型的参数，则返回 MapperMethod.ParameterMap 类型。
     *
     * @param method 要获取参数类型的方法
     * @return 方法的参数类型
     */
    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> clazz : parameterTypes) {
            // 排除 RowBounds 和 ResultHandler 类型的参数
            if (!RowBounds.class.isAssignableFrom(clazz) && !ResultHandler.class.isAssignableFrom(clazz)) {
                if (parameterType == null) {
                    parameterType = clazz;
                } else {
                    // 多个参数或者有 RowBounds 或 ResultHandler 类型的参数，则返回 ParameterMap 类型
                    parameterType = MapperMethod.ParameterMap.class;
                }
            }
        }
        return parameterType;
    }

}
