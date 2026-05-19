package com.mall.dto;

import lombok.Data;

@Data
public class CreateReviewRequest {
    private Long productId;
    private Integer rating;
    private String content;
    private String images;
}
