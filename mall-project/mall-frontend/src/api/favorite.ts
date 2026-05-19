import request from './request';

// 添加收藏
export const addFavorite = (productId: number) => {
  return request.post(`/favorites/${productId}`);
};

// 取消收藏
export const removeFavorite = (productId: number) => {
  return request.delete(`/favorites/${productId}`);
};

// 获取收藏列表
export const getFavoriteList = (page = 1, pageSize = 10) => {
  return request.get('/favorites', {
    params: { page, pageSize }
  });
};

// 检查收藏状态
export const checkFavorite = (productId: number) => {
  return request.get(`/favorites/check/${productId}`);
};

// 获取收藏数量
export const getFavoriteCount = () => {
  return request.get('/favorites/count');
};
