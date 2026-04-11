package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mall.dto.FavoriteDTO;
import com.mall.entity.Favorite;
import com.mall.mapper.FavoriteMapper;
import com.mall.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public Favorite addFavorite(Long userId, Long productId) {
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("product_id", productId);
        Favorite existingFavorite = favoriteMapper.selectOne(queryWrapper);
        if (existingFavorite != null) {
            return existingFavorite;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
        return favorite;
    }

    @Override
    public void removeFavorite(Long userId, Long productId) {
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("product_id", productId);
        favoriteMapper.delete(queryWrapper);
    }

    @Override
    public List<FavoriteDTO> getFavoriteList(Long userId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        List<Favorite> favorites = favoriteMapper.getUserFavorites(userId, offset, pageSize);
        return convertToDTOList(favorites);
    }

    @Override
    public boolean checkFavorite(Long userId, Long productId) {
        Integer count = favoriteMapper.checkFavorite(userId, productId);
        return count > 0;
    }

    @Override
    public Integer getFavoriteCount(Long userId) {
        return favoriteMapper.getFavoriteCount(userId);
    }

    private List<FavoriteDTO> convertToDTOList(List<Favorite> favorites) {
        List<FavoriteDTO> favoriteDTOs = new ArrayList<>();
        for (Favorite favorite : favorites) {
            FavoriteDTO dto = new FavoriteDTO();
            dto.setId(favorite.getId());
            dto.setUserId(favorite.getUserId());
            dto.setProductId(favorite.getProductId());
            dto.setCreateTime(favorite.getCreateTime());
            favoriteDTOs.add(dto);
        }
        return favoriteDTOs;
    }
}
