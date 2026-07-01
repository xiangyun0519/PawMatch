package com.petadopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadopt.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
