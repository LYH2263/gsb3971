package com.hanyu.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hanyu.learning.domain.entity.RoomEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoomMapper extends BaseMapper<RoomEntity> {
}
