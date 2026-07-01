package com.petadopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadopt.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
