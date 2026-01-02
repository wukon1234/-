package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话会话Mapper接口
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
