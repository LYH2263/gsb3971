package com.hanyu.learning.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("customer_service_focus")
public class CustomerServiceFocusEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private String serviceName;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private String serviceStatus;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
}

