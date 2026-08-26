package com.hanyu.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hanyu.learning.domain.entity.CustomerServiceObjectEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerServiceObjectMapper extends BaseMapper<CustomerServiceObjectEntity> {
}

