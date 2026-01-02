package com.mall.common;

/**
 * 响应状态码枚举
 */
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 业务错误码
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXIST(1003, "用户已存在"),
    
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "商品库存不足"),
    
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    
    ASSISTANT_ERROR(4001, "智能助手服务异常");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
