package com.code.script.xmltags;

import com.code.parsing.GenericTokenParser;
import com.code.parsing.TokenHandler;
import com.code.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * 文本SQL节点(CDATA|TEXT)
 *
 * @author HeXin
 * @date 2024/02/20
 */
public class TextSqlNode implements SqlNode{

    /**
     * 文本
     */
    private String text;

    /**
     * 注入过滤器
     */
    private Pattern injectionFilter;

    /**
     * 动态检查令牌解析器
     *
     * @author HeXin
     * @date 2024/02/20
     */
    private static class DynamicCheckerTokenParser implements TokenHandler {
        /**
         * 是否为动态 SQL
         */
        private boolean isDynamic;

        public DynamicCheckerTokenParser() {

        }

        public boolean isDynamic() {
            return isDynamic;
        }

        @Override
        public String handleToken(String content) {
            // 设置 isDynamic 为 true，即调用该类则必定是动态 SQL
            this.isDynamic = true;
            return null;
        }
    }

    private static class BindingTokenParser implements TokenHandler {

        private DynamicContext context;

        private Pattern  injectionFilter;

        public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
            this.context = context;
            this.injectionFilter = injectionFilter;
        }

        /**
         * 检查注入数据
         *
         * @param value 值
         */
        private void checkInjection(String value) {
            if(injectionFilter != null && !injectionFilter.matcher(value).matches()) {
                throw new RuntimeException("无效的输入。请符合正则表达式：" + injectionFilter.pattern());
            }
        }

        @Override
        public String handleToken(String content) {
            Object parameter = context.getBindings().get("_parameter");
            if(parameter == null) {
                context.getBindings().put("value",null);
            } else if(SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
                context.getBindings().put("vaule",parameter);
            }
            // 从缓存中获取值
            Object value = OgnlCache.getValue(content, context.getBindings());
            // 用空字符串替代null值
            String strValue = (value == null ? "" : String.valueOf(value));
            checkInjection(strValue);
            return strValue;
        }
    }

    public TextSqlNode(String text, Pattern injectionFilter) {
        this.text = text;
        this.injectionFilter = injectionFilter;
    }

    public TextSqlNode(String text) {
        this.text = text;
    }

    /**
     * 是否为动态 SQL
     *
     * @return boolean
     */
    public boolean isDynamic() {
        DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
        GenericTokenParser parser = createParser(checker);
        parser.parse(text);
        return checker.isDynamic();
    }

    /**
     * 创建解析器
     *
     * @param checker 检查程序
     * @return {@link GenericTokenParser}
     */
    private GenericTokenParser createParser(TokenHandler checker) {
        return new GenericTokenParser("${","}",checker);
    }

    @Override
    public boolean apply(DynamicContext context) {
        GenericTokenParser parser = createParser(new BindingTokenParser(context,injectionFilter));
        context.appendSql(parser.parse(text));
        return true;
    }
}
