package com.petadopt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petadopt.entity.PetProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends BaseMapper<PetProfile> {
}
