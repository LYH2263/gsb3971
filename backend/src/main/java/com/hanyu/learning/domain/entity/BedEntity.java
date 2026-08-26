package com.hanyu.learning.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("beds")
public class BedEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String bedNo;
    private String status;
}
