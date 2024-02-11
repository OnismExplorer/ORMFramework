package com.code.builder;

import java.util.HashMap;

/**
 * 参数表达式
 *
 * @author HeXin
 * @date 2024/02/05
 */
public class ParameterExpression extends HashMap <String,String>{

    private static final long serialVersionUID = 988290288192832756L;

    public ParameterExpression(String experssion) {
        parse(experssion);
    }

    /**
     * 解析
     *
     * @param expression 表达式
     */
    private void parse(String expression) {
        // 首先会去除空白，返回的 index 是第一个不是空白的字符位置
        int index = skipWS(expression,0);
        if(expression.charAt(index) == '(') {
            // 处理表达式
            expression(expression,index + 1);
        } else {
            // 处理属性
            property(expression,index);
        }
    }

    /**
     * 解析表达式
     *
     * @param expression 表达式
     * @param index      指数
     */
    private void expression(String expression,int index) {
        int match = 1;
        int right = index + 1;
        while(match > 0) {
            if(expression.charAt(right) == ')') {
                match--;
            } else if(expression.charAt(right) == '('){
                match++;
            }
            right++;
        }
        put("expression",expression.substring(index,right - 1));
        jdbcTypeOpt(expression,right);
    }

    /**
     * 解析配置
     *
     * @param expression 表达式
     * @param index      位置索引
     */
    private void property(String expression,int index) {
        if(index < expression.length()) {
            // 首先得到逗号或冒号之前的字符串，将其加入property
            int right = skipUntil(expression,index,",:");
            put("property",trimmeStr(expression,index,right));
            // 处理 javaType
            jdbcTypeOpt(expression,right);
        }
    }

    /**
     * 跳过表达式中的空白字符，返回下一个非空白字符的索引位置。
     *
     * @param experssion 表达式字符串
     * @param index 当前位置索引
     * @return 下一个非空白字符的索引位置
     */
    private int skipWS(String experssion,int index) {
        // 从当前位置开始遍历表达式，找到第一个非空白字符的索引位置
        for(int i = index;i < experssion.length();i++) {
            if(experssion.charAt(i) > 0x20) {
                return i;
            }
        }

        // 如果表达式已经遍历结束，则直接返回表达式长度
        return experssion.length();
    }

    /**
     * 跳过表达式中的字符，直到遇到指定的结束字符串中的字符为止，返回结束字符的索引位置。
     *
     * @param expression 表达式字符串
     * @param index 当前位置索引
     * @param endStr 结束字符串
     * @return 结束字符的索引位置
     */
    private int skipUntil(String expression, int index, final String endStr) {
        // 从当前位置开始遍历表达式，直到遇到结束字符串中的字符为止
        for (int i = index; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (endStr.indexOf(c) > -1) {
                return i;
            }
        }
        // 如果表达式已经遍历结束，则返回表达式的长度
        return expression.length();
    }

    /**
     * 处理表达式中的类型信息，包括 javaType 和 jdbcType。
     *
     * @param expression 表达式字符串
     * @param index 当前位置索引
     */
    private void jdbcTypeOpt(String expression,int index) {
        // 首先去除空白字符，返回 index 是第一个不是空白的字符位置
        index = skipWS(expression,index);
        if(index < expression.length()) {
            // 第一个  property 解析完后会有两种情况，逗号与冒号
            if(expression.charAt(index) == ';') {
                // 解析 jdbcType
                jdbcTypeOpt(expression,index + 1);
            } else if(expression.charAt(index) == ',') {
                // 解析 option
                option(expression, index + 1);
            } else {
                // 抛出解析错误异常
                throw new RuntimeException("在 {" + expression + "} 的位置 " + index + " 处发生解析错误。");
            }
        }
    }

    /**
     * 解析表达式中的 jdbcType 类型信息，并处理后续的选项信息。
     *
     * @param expression 表达式字符串
     * @param index 当前位置索引
     */
    private void jdbcType(String expression,int index) {
        // 找到非空白字符的起始位置
        int left = skipWS(expression,index);
        // 找到逗号之前的结束位置
        int right = skipUntil(expression,left,",");
        if(right > left) {
            put("jdbcType",trimmeStr(expression,left,right));
        } else {
            throw new RuntimeException("在 {" + expression + "} 的位置 " + index + " 处发生解析错误。");
        }
        option(expression,right + 1);
    }

    /**
     * 解析表达式中的选项信息，并递归调用自身解析后续的选项信息。
     *
     * @param expression 表达式
     * @param index  当前位置索引
     */
    private void option(String expression,int index) {
        // 找到非空白字符的起始位置
        int left = skipWS(expression,index);
        if(left < expression.length()) {
            // 找到等号之前的结束位置，解析选项名
            int right = skipUntil(expression,left,"=");
            String name = trimmeStr(expression,left,right);

            // 移动到等号之后，找到逗号之前的结束位置，解析选项值
            left = right + 1;
            right =  skipUntil(expression,left,",");
            String value = trimmeStr(expression,left,right);
            put(name,value);
            // 递归调用option，进行逗号后面一个属性的解析
            option(expression,right + 1);
        }
    }

    /**
     * 对字符串进行修整，去除起始和结束位置的空白字符，并返回修整后的字符串。
     *
     * @param str 待修整的字符串
     * @param start 修整的起始位置
     * @param end 修整的结束位置
     * @return 修整后的字符串
     */
    private String trimmeStr(String str, int start, int end) {
        // 去除起始位置的空白字符
        while (str.charAt(start) <= 0x20) {
            start++;
        }

        // 去除结束位置的空白字符
        while (str.charAt(end - 1) <= 0x20) {
            end--;
        }

        // 返回修整后的字符串
        return start >= end ? "" : str.substring(start, end);
    }
}
