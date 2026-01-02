package com.mall.dto;

import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class UpdatePasswordRequest {
    
    /**
     * 原密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
}
