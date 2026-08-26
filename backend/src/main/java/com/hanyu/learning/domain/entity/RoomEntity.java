package com.hanyu.learning.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("rooms")
public class RoomEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer floor;
    private String roomNo;
    private Integer status;
}
