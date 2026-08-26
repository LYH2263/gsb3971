package com.hanyu.learning.dto.view;

import java.time.LocalDate;

public record WeeklyMenuView(
    LocalDate weekStartDate,
    String mon,
    String tue,
    String wed,
    String thu,
    String fri,
    String sat,
    String sun
) {
}
