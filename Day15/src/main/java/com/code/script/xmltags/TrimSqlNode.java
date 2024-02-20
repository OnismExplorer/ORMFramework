package com.code.script.xmltags;

import com.code.session.Configuration;

import java.util.*;

/**
 * trim Sql Node 节点解析
 *
 * @author HeXin
 * @date 2024/02/20
 */
public class TrimSqlNode implements SqlNode{

    private SqlNode contents;

    private String prefix;

    private String suffix;

    private List<String> prefixesToOverride;

    private List<String> suffixesToOverride;

    private Configuration configuration;

    /**
     * 过滤后动态上下文
     *
     * @author HeXin
     * @date 2024/02/20
     */
    private class FilteredDynamicContext extends DynamicContext {

        private DynamicContext context;

        /**
         * 前缀应用
         */
        private boolean prefixApplied;

        /**
         * 后缀应用
         */
        private boolean suffixApplied;

        /**
         * sql 建造者
         */
        private StringBuilder sqlBuilder;

        public FilteredDynamicContext(DynamicContext context) {
            super(configuration, null);
            this.context = context;
            this.prefixApplied = false;
            this.suffixApplied = false;
            this.sqlBuilder = new StringBuilder();
        }

        public void applyAll() {
            sqlBuilder = new StringBuilder(sqlBuilder.toString().trim());
            String trimmedUppercaseSql = sqlBuilder.toString().toUpperCase(Locale.ENGLISH);
            if(trimmedUppercaseSql.length() > 0) {
                applyPrefix(sqlBuilder,trimmedUppercaseSql);
                applySuffix(sqlBuilder,trimmedUppercaseSql);
            }
            context.appendSql(sqlBuilder.toString());
        }

        /**
         * 应用前缀
         *
         * @param builder             构建器
         * @param trimmedUppercaseSql 精简大写SQL
         */
        private void applyPrefix(StringBuilder builder, String trimmedUppercaseSql) {
            if(!prefixApplied) {
                prefixApplied = true;
                if(prefixesToOverride != null) {
                    for (String toRemove : prefixesToOverride) {
                        if(trimmedUppercaseSql.startsWith(toRemove)) {
                            builder.delete(0,toRemove.trim().length());
                        }
                    }
                }
                if(prefix != null) {
                    builder.insert(0," ");
                    builder.insert(0,prefix);
                }
            }
        }

        /**
         * 应用后缀
         *
         * @param builder             构建器
         * @param trimmedUppercaseSql 精简大写SQL
         */
        private void applySuffix(StringBuilder builder, String trimmedUppercaseSql) {
            if (!suffixApplied) {
                suffixApplied = true;
                if (suffixesToOverride != null) {
                    for (String toRemove : suffixesToOverride) {
                        if (trimmedUppercaseSql.endsWith(toRemove) || trimmedUppercaseSql.endsWith(toRemove.trim())) {
                            int start = builder.length() - toRemove.trim().length();
                            int end = builder.length();
                            builder.delete(start, end);
                            break;
                        }
                    }
                }
                if (suffix != null) {
                    builder.append(" ");
                    builder.append(suffix);
                }
            }
        }

        @Override
        public Map<String, Object> getBindings() {
            return context.getBindings();
        }

        @Override
        public void bind(String name, Object value) {
            context.bind(name, value);
        }

        @Override
        public int getUniqueNumber() {
            return context.getUniqueNumber();
        }

        @Override
        public void appendSql(String sql) {
            sqlBuilder.append(sql);
        }

        @Override
        public String getSql() {
            return context.getSql();
        }
    }

    /**
     * 解析覆盖
     *
     * @param override 要覆盖后缀
     * @return {@link List}<{@link String}>
     */
    private static List<String> parseOverride(String override) {
        if(override != null) {
            final StringTokenizer parser = new StringTokenizer(override,"|",false);
            final List<String> list = new ArrayList<>(parser.countTokens());
            while(parser.hasMoreTokens()) {
                list.add(parser.nextToken().toUpperCase(Locale.ENGLISH));
            }
            return list;
        }
        return Collections.emptyList();
    }

    public TrimSqlNode(Configuration configuration, SqlNode contents, String prefix, String prefixesToOverride, String suffix, String suffixesToOverride) {
        this(contents, prefix,suffix, parseOverride(prefixesToOverride), parseOverride(suffixesToOverride),configuration);
    }

    protected TrimSqlNode(SqlNode contents, String prefix, String suffix, List<String> prefixesToOverride, List<String> suffixesToOverride, Configuration configuration) {
        this.contents = contents;
        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixesToOverride = prefixesToOverride;
        this.suffixesToOverride = suffixesToOverride;
        this.configuration = configuration;
    }

    @Override
    public boolean apply(DynamicContext context) {
        FilteredDynamicContext filteredDynamicContext = new FilteredDynamicContext(context);
        boolean result = contents.apply(filteredDynamicContext);
        filteredDynamicContext.applyAll();
        return result;
    }
}
