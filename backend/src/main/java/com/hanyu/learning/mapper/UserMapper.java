package com.hanyu.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hanyu.learning.domain.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
