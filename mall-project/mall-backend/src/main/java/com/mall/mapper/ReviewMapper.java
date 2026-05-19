package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    @Select("SELECT AVG(rating) FROM review WHERE product_id = #{productId}")
    Double getAverageRating(Long productId);

    @Select("SELECT COUNT(*) FROM review WHERE product_id = #{productId}")
    Integer getReviewCount(Long productId);

    @Select("SELECT r.*, u.username FROM review r LEFT JOIN user u ON r.user_id = u.id WHERE r.product_id = #{productId} ORDER BY r.create_time DESC LIMIT #{offset}, #{limit}")
    List<Review> getProductReviews(Long productId, Integer offset, Integer limit);

    @Select("SELECT r.*, u.username FROM review r LEFT JOIN user u ON r.user_id = u.id WHERE r.user_id = #{userId} ORDER BY r.create_time DESC LIMIT #{offset}, #{limit}")
    List<Review> getUserReviews(Long userId, Integer offset, Integer limit);
}
