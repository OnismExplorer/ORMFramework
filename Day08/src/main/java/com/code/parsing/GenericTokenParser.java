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

        // 如果文本不为空且长度大于 0
        if (text != null && text.length() > 0) {
            char[] src = text.toCharArray();
            int offset = 0;
            int start = text.indexOf(openToken, offset);

            while (start > -1) {
                // 如果占位符前有转义符
                if (start > 0 && src[start - 1] == '\\') {
                    // 将转义符和占位符之间的内容添加到 builder 中，继续下一个循环
                    builder.append(src, offset, start - offset - 1).append(openToken);
                    offset = start + openToken.length();
                } else {
                    // 寻找占位符的结束位置
                    int end = text.indexOf(closeToken, start);

                    // 如果找不到结束位置，将 offset 到文本末尾的内容添加到 builder 中，结束循环
                    if (end == -1) {
                        builder.append(src, offset, src.length - offset);
                        offset = src.length;
                    } else {
                        // 将 offset 到占位符开始位置的内容添加到 builder 中
                        builder.append(src, offset, start - offset);
                        offset = start + openToken.length();

                        // 获取占位符的内容，使用处理器处理，并将结果添加到 builder 中
                        String content = new String(src, offset, end - offset);
                        builder.append(handler.handleToken(content));

                        // 更新 offset 到占位符结束位置的位置
                        offset = end + closeToken.length();
                    }
                }

                // 寻找下一个占位符的开始位置
                start = text.indexOf(openToken, offset);
            }

            // 如果 offset 小于 src.length，将 offset 到文本末尾的内容添加到 builder 中
            if (offset < src.length) {
                builder.append(src, offset, src.length - offset);
            }
        }

        // 返回解析后的文本
        return builder.toString();
    }

}
