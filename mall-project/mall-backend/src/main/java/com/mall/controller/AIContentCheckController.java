package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.AIContentCheckRequestDTO;
import com.mall.dto.AIContentCheckResponseDTO;
import com.mall.service.AIContentCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI内容检测控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai/content-check")
public class AIContentCheckController {

    @Autowired
    private AIContentCheckService aiContentCheckService;

    /**
     * 检测内容是否违规
     */
    @PostMapping
    public Result<AIContentCheckResponseDTO> checkContent(@RequestBody AIContentCheckRequestDTO request) {
        try {
            AIContentCheckResponseDTO result = aiContentCheckService.checkContent(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("内容检测失败: {}", e.getMessage());
            return Result.error("内容检测失败，请稍后重试");
        }
    }
}
