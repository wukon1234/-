package com.mall.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long productId;
    private Integer rating;
    private String content;
    private String images;
    private LocalDateTime createTime;
}
