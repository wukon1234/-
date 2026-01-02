package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.ConversationMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话消息Mapper接口
 */
@Mapper
public interface ConversationMessageMapper extends BaseMapper<ConversationMessage> {
}
