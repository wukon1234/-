package com.mall.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private Double productPrice;
    private LocalDateTime createTime;
}
