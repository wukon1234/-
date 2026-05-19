package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    Integer checkFavorite(Long userId, Long productId);

    @Select("SELECT f.*, p.name as product_name, p.image_url as product_image, p.price as product_price FROM favorite f LEFT JOIN product p ON f.product_id = p.id WHERE f.user_id = #{userId} ORDER BY f.create_time DESC LIMIT #{offset}, #{limit}")
    List<Favorite> getUserFavorites(Long userId, Integer offset, Integer limit);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId}")
    Integer getFavoriteCount(Long userId);
}
