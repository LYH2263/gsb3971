package com.hanyu.learning.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import lombok.Data;

@Data
@TableName("customer_meal_plan")
public class CustomerMealPlanEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private LocalDate weekStartDate;
    private String mealType;
    private String dietTaboo;
    private String note;
    private Long createdBy;
}
