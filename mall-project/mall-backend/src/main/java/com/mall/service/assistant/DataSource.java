package com.mall.service.assistant;

/**
 * RAG数据源类型枚举
 */
public enum DataSource {
    
    /**
     * 商品数据源
     */
    PRODUCT("product", "商品"),
    
    /**
     * 订单数据源
     */
    ORDER("order", "订单"),
    
    /**
     * 用户数据源
     */
    USER("user", "用户"),
    
    /**
     * 自定义数据源
     */
    CUSTOM("custom", "自定义");
    
    private final String code;
    private final String name;
    
    DataSource(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static DataSource fromCode(String code) {
        for (DataSource ds : values()) {
            if (ds.code.equals(code)) {
                return ds;
            }
        }
        return CUSTOM;
    }
}

