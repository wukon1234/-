package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private String avatar;
    
    private Integer status;  // 1-正常 0-禁用
    
    private Integer role;  // 1-普通用户 2-管理员
}
