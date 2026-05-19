import request from './request';

// 创建评价
export const createReview = (data: {
  productId: number;
  rating: number;
  content: string;
  images?: string;
}) => {
  return request.post('/reviews', data);
};

// 获取商品评价
export const getProductReviews = (productId: number, page = 1, pageSize = 10) => {
  return request.get(`/reviews/product/${productId}`, {
    params: { page, pageSize }
  });
};

// 获取商品平均评分
export const getAverageRating = (productId: number) => {
  return request.get(`/reviews/product/${productId}/rating`);
};

// 获取商品评价数量
export const getReviewCount = (productId: number) => {
  return request.get(`/reviews/product/${productId}/count`);
};

// 获取用户评价
export const getUserReviews = (page = 1, pageSize = 10) => {
  return request.get('/reviews/user', {
    params: { page, pageSize }
  });
};

// 删除评价
export const deleteReview = (reviewId: number) => {
  return request.delete(`/reviews/${reviewId}`);
};
