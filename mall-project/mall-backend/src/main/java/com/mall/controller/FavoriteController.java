package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.FavoriteDTO;
import com.mall.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public Result addFavorite(@PathVariable Long productId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        favoriteService.addFavorite(userId, productId);
        return Result.success(null);
    }

    @DeleteMapping("/{productId}")
    public Result removeFavorite(@PathVariable Long productId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        favoriteService.removeFavorite(userId, productId);
        return Result.success(null);
    }

    @GetMapping
    public Result getFavoriteList(@RequestParam(defaultValue = "1") Integer page, 
                                 @RequestParam(defaultValue = "10") Integer pageSize, 
                                 HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<FavoriteDTO> favorites = favoriteService.getFavoriteList(userId, page, pageSize);
        return Result.success(favorites);
    }

    @GetMapping("/check/{productId}")
    public Result checkFavorite(@PathVariable Long productId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean isFavorite = favoriteService.checkFavorite(userId, productId);
        return Result.success(isFavorite);
    }

    @GetMapping("/count")
    public Result getFavoriteCount(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Integer count = favoriteService.getFavoriteCount(userId);
        return Result.success(count);
    }
}
