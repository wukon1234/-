package com.mall.service;

import com.mall.dto.AIContentCheckRequestDTO;
import com.mall.dto.AIContentCheckResponseDTO;

/**
 * AI内容检测服务接口
 */
public interface AIContentCheckService {
    /**
     * 检测内容是否违规
     * @param request 检测请求
     * @return 检测结果
     */
    AIContentCheckResponseDTO checkContent(AIContentCheckRequestDTO request);
    
    /**
     * 快速检测内容是否违规（仅返回是否违规）
     * @param content 待检测的文本内容
     * @return 是否违规
     */
    boolean isContentViolation(String content);
}
