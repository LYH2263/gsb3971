package com.hanyu.learning.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("customer_service_object")
public class CustomerServiceObjectEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long managerUserId;
    private LocalDateTime assignedAt;
}

