package com.code.parsing;

/**
 * 通用记号处理器(处理 #{} 和 ${})
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class GenericTokenParser {
    private final String openToken;

    /**
     * 密切令牌
     */
    private final String closeToken;

    /**
     * 记号处理程序
     */
    private final TokenHandler handler;

    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    /**
     * 解析文本中的占位符，并使用指定的处理器处理每个占位符的内容。
     *
     * @param text 待解析的文本
     * @return 解析后的文本
     */
    public String parse(String text) {
        StringBuilder builder = new StringBuilder();
        if (text != null && text.length() > 0) {
            char[] src = text.toCharArray();
            int offset = 0;
            int start = text.indexOf(openToken, offset);
            // #{favouriteSection,jdbcType=VARCHAR}
            // 这里是循环解析参数，参考GenericTokenParserTest,比如可以解析${first_name} ${initial} ${last_name} reporting.这样的字符串,里面有3个${}
            while (start > -1) {
                //判断一下 ${ 前面是否是反斜杠，这个逻辑在老版的mybatis中（如3.1.0）是没有的
                if (start > 0 && src[start - 1] == '\\') {
                    // the variable is escaped. remove the backslash.
                    // 新版已经没有调用substring了，改为调用如下的offset方式，提高了效率
                    builder.append(src, offset, start - offset - 1).append(openToken);
                    offset = start + openToken.length();
                } else {
                    int end = text.indexOf(closeToken, start);
                    if (end == -1) {
                        builder.append(src, offset, src.length - offset);
                        offset = src.length;
                    } else {
                        builder.append(src, offset, start - offset);
                        offset = start + openToken.length();
                        String content = new String(src, offset, end - offset);
                        // 得到一对大括号里的字符串后，调用handler.handleToken,比如替换变量这种功能
                        builder.append(handler.handleToken(content));
                        offset = end + closeToken.length();
                    }
                }
                start = text.indexOf(openToken, offset);
            }
            if (offset < src.length) {
                builder.append(src, offset, src.length - offset);
            }
        }
        return builder.toString();
    }

}
