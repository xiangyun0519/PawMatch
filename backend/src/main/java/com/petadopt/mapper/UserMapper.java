package com.petadopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadopt.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
