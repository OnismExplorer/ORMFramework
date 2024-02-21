package com.code.executor.keygen;

import com.code.executor.Executor;
import com.code.mapping.MappedStatement;
import com.code.reflection.MetaObject;
import com.code.session.Configuration;
import com.code.session.RowBounds;

import java.sql.Statement;
import java.util.List;

/**
 * 键值生成器
 *
 * @author HeXin
 * @date 2024/02/18
 */
public class SelectKeyGenerator implements KeyGenerator{

    /**
     * 选择密钥后缀
     */
    public static final String SELECT_KEY_SUFFIX = "!selectKey";

    /**
     * 是否为顺序(非自增)主键
     */
    private boolean executorBefore;

    /**
     * 关键语句
     */
    private MappedStatement keyStatement;

    public SelectKeyGenerator(boolean executorBefore, MappedStatement keyStatement) {
        this.executorBefore = executorBefore;
        this.keyStatement = keyStatement;
    }

    @Override
    public void processBefore(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {
        if(executorBefore) {
            processGenerateKeys(executor,mappedStatement,parameter);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {
        if(!executorBefore) {
            processGenerateKeys(executor,mappedStatement,parameter);
        }
    }

    /**
     * 处理生成主键
     *
     * @param executor        执行器
     * @param mappedStatement 映射语句
     * @param parameter       参数
     */
    private void processGenerateKeys(Executor executor, MappedStatement mappedStatement, Object parameter) {
        try {
            // 检查参数和 keyStatement 是否为空
            if (parameter != null && keyStatement != null) {
                String[] keyProperties = keyStatement.getKeyProperties();
                if (keyProperties != null) {
                    // 获取 MyBatis 配置信息
                    final Configuration configuration = mappedStatement.getConfiguration();
                    // 创建参数对象的 MetaObject
                    final MetaObject metaParam = configuration.newMetaObject(parameter);
                    // 创建专门用于执行 keyStatement 的 Executor
                    Executor keyExecutor = configuration.newExecutor(executor.getTransaction());

                    // 执行 keyStatement 查询，获取结果列表
                    List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);

                    // 处理结果列表
                    if (values.isEmpty()) {
                        throw new RuntimeException("SelectKey语句未返回任何结果");
                    } else if (values.size() > 1) {
                        // 如果返回结果大于 1，则处理多个属性的情况
                        MetaObject metaResult = configuration.newMetaObject(values.get(0));

                        if (keyProperties.length == 1) {
                            // 处理单个属性的情况
                            if (metaResult.hasGetter(keyProperties[0])) {
                                setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                            } else {
                                setValue(metaParam, keyProperties[0], values.get(0));
                            }
                        } else {
                            // 处理多个属性的情况
                            handleMultipleProperties(keyProperties, metaParam, metaResult);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 捕捉异常，抛出带有详细信息的运行时异常
            throw new RuntimeException("选择键或设置结果到参数对象时发生错误：" + e);
        }
    }

    /**
     * 设置值
     *
     * @param metaParam   元参数
     * @param keyProperty 键属性
     * @param value       价值
     */
    private void setValue(MetaObject metaParam, String keyProperty, Object value) {
        if(metaParam.hasSetter(keyProperty)) {
            metaParam.setValue(keyProperty,value);
        }
        throw new RuntimeException("在 " + metaParam.getOriginalObject().getClass().getName() + " 中找不到 keyProperty '" + keyProperty + "' 的 setter 方法。");

    }

    /**
     * 处理多个属性
     *
     * @param keyProperties 关键属性
     * @param metaParam     元参数
     * @param metaResult    元结果
     */
    private void handleMultipleProperties(String[] keyProperties, MetaObject metaParam, MetaObject metaResult) {
        String[] keyColumns = keyStatement.getKeyColumns();

        if(keyColumns == null || keyColumns.length == 0) {
            for (String keyProperty : keyProperties) {
                setValue(metaParam,keyProperty,metaResult.getValue(keyProperty));
            }
        } else {
            if(keyProperties.length != keyColumns.length) {
                throw new RuntimeException("如果 SelectKey 包含 key columns，则其数量必须与 key properties 的数量相匹配。");
            }
            for(int i = 0;i < keyProperties.length;i++) {
                setValue(metaParam,keyProperties[i],metaResult.getValue(keyColumns[i]));
            }
        }
    }

}
