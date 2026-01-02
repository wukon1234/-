package com.mall.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话DTO
 */
@Data
public class ConversationDTO {
    
    private Long id;
    
    private String sessionId;
    
    private String title;
    
    private String lastMessage;
    
    private LocalDateTime updateTime;
}

