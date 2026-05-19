package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mall.dto.CreateReviewRequest;
import com.mall.dto.ReviewDTO;
import com.mall.entity.Review;
import com.mall.mapper.ReviewMapper;
import com.mall.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public Review createReview(Long userId, CreateReviewRequest request) {
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("product_id", request.getProductId());
        Review existingReview = reviewMapper.selectOne(queryWrapper);
        if (existingReview != null) {
            existingReview.setRating(request.getRating());
            existingReview.setContent(request.getContent());
            existingReview.setImages(request.getImages());
            reviewMapper.updateById(existingReview);
            return existingReview;
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        reviewMapper.insert(review);
        return review;
    }

    @Override
    public List<ReviewDTO> getProductReviews(Long productId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        List<Review> reviews = reviewMapper.getProductReviews(productId, offset, pageSize);
        return convertToDTOList(reviews);
    }

    @Override
    public Double getAverageRating(Long productId) {
        return reviewMapper.getAverageRating(productId);
    }

    @Override
    public Integer getReviewCount(Long productId) {
        return reviewMapper.getReviewCount(productId);
    }

    @Override
    public List<ReviewDTO> getUserReviews(Long userId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        List<Review> reviews = reviewMapper.getUserReviews(userId, offset, pageSize);
        return convertToDTOList(reviews);
    }

    @Override
    public void deleteReview(Long userId, Long reviewId) {
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", reviewId)
                .eq("user_id", userId);
        reviewMapper.delete(queryWrapper);
    }

    private List<ReviewDTO> convertToDTOList(List<Review> reviews) {
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        for (Review review : reviews) {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setUserId(review.getUserId());
            dto.setProductId(review.getProductId());
            dto.setRating(review.getRating());
            dto.setContent(review.getContent());
            dto.setImages(review.getImages());
            dto.setCreateTime(review.getCreateTime());
            reviewDTOs.add(dto);
        }
        return reviewDTOs;
    }
}
