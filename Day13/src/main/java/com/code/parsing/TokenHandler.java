package com.code.parsing;

/**
 * 标记处理器
 *
 * @author HeXin
 * @date 2024/02/06
 */
public interface TokenHandler {

    /**
     * 处理令牌
     *
     * @param content 内容
     * @return {@link String}
     */
    String handleToken(String content);
}
