package com.hanyu.learning.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("meal_weekly_menu")
public class MealWeeklyMenuEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate weekStartDate;
    private String mon;
    private String tue;
    private String wed;
    private String thu;
    private String fri;
    private String sat;
    private String sun;
    private LocalDateTime updatedAt;
}
