package com.mall.service;

import com.mall.dto.CreateReviewRequest;
import com.mall.dto.ReviewDTO;
import com.mall.entity.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(Long userId, CreateReviewRequest request);
    List<ReviewDTO> getProductReviews(Long productId, Integer page, Integer pageSize);
    Double getAverageRating(Long productId);
    Integer getReviewCount(Long productId);
    List<ReviewDTO> getUserReviews(Long userId, Integer page, Integer pageSize);
    void deleteReview(Long userId, Long reviewId);
}
