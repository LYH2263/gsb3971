package com.hanyu.learning.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("customers")
public class CustomerEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private Integer age;
    private Integer gender;
    private String status;
    private Long bedId;
    private LocalDate checkinDate;
    private String note;
    private LocalDateTime createdAt;
}
