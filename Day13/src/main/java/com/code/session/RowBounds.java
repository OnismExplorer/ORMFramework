package com.code.session;

/**
 * 分页记录限制
 *
 * @author HeXin
 * @date 2024/02/12
 */
public class RowBounds {
    /**
     * 无行偏移
     */
    public static final int NO_ROW_OFFSET = 0;

    /**
     * 无行限制
     */
    public static final int NO_ROW_LIMIT = Integer.MAX_VALUE;

    /**
     * 默认
     */
    public static final RowBounds DEFAULT = new RowBounds();

    /**
     * 偏移量
     */
    private int offset;

    /**
     * 限制
     */
    private int limit;

    public RowBounds() {
        this.offset = NO_ROW_OFFSET;
        this.limit = NO_ROW_LIMIT;
    }

    public RowBounds(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }
}
