package com.hanyu.learning.dto.view;

import java.time.LocalDate;

public record CustomerView(
    Long id,
    String name,
    String phone,
    Integer age,
    Integer gender,
    String status,
    Long bedId,
    String bedNo,
    String roomNo,
    LocalDate checkinDate,
    String note
) {
}
