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
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        char[] src = text.toCharArray();
        int offset = 0;
        boolean foundToken = false;

        while (offset < src.length) {
            int start = text.indexOf(openToken, offset);

            if (start == -1) {
                // 没有找到占位符，将剩余文本添加到 builder 中并结束循环
                builder.append(src, offset, src.length - offset);
            } else {
                // 处理占位符前的内容
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();

                int end = text.indexOf(closeToken, offset);

                if (end == -1) {
                    // 没有找到占位符的结束位置，将剩余文本添加到 builder 中并结束循环
                    builder.append(src, offset, src.length - offset);
                } else {
                    // 获取占位符的内容，使用处理器处理，并将结果添加到 builder 中
                    String content = new String(src, offset, end - offset);
                    builder.append(handler.handleToken(content));

                    // 更新 offset 到占位符结束位置的位置
                    offset = end + closeToken.length();
                    foundToken = true;
                }
            }

            // 如果找到了占位符，结束循环
            if (foundToken) {
                break;
            }
        }

        return builder.toString();
    }

}
