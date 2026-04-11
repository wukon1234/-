package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.CreateReviewRequest;
import com.mall.dto.ReviewDTO;
import com.mall.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public Result createReview(@RequestBody CreateReviewRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        reviewService.createReview(userId, request);
        return Result.success(null);
    }

    @GetMapping("/product/{productId}")
    public Result getProductReviews(@PathVariable Long productId, 
                                   @RequestParam(defaultValue = "1") Integer page, 
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        List<ReviewDTO> reviews = reviewService.getProductReviews(productId, page, pageSize);
        return Result.success(reviews);
    }

    @GetMapping("/product/{productId}/rating")
    public Result getAverageRating(@PathVariable Long productId) {
        Double rating = reviewService.getAverageRating(productId);
        return Result.success(rating);
    }

    @GetMapping("/product/{productId}/count")
    public Result getReviewCount(@PathVariable Long productId) {
        Integer count = reviewService.getReviewCount(productId);
        return Result.success(count);
    }

    @GetMapping("/user")
    public Result getUserReviews(@RequestParam(defaultValue = "1") Integer page, 
                                @RequestParam(defaultValue = "10") Integer pageSize, 
                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ReviewDTO> reviews = reviewService.getUserReviews(userId, page, pageSize);
        return Result.success(reviews);
    }

    @DeleteMapping("/{reviewId}")
    public Result deleteReview(@PathVariable Long reviewId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        reviewService.deleteReview(userId, reviewId);
        return Result.success(null);
    }
}
