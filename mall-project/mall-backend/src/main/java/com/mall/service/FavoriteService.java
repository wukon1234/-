package com.mall.service;

import com.mall.dto.FavoriteDTO;
import com.mall.entity.Favorite;

import java.util.List;

public interface FavoriteService {
    Favorite addFavorite(Long userId, Long productId);
    void removeFavorite(Long userId, Long productId);
    List<FavoriteDTO> getFavoriteList(Long userId, Integer page, Integer pageSize);
    boolean checkFavorite(Long userId, Long productId);
    Integer getFavoriteCount(Long userId);
}
