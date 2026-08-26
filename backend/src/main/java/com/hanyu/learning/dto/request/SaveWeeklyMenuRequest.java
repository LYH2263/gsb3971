package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.Size;

public record SaveWeeklyMenuRequest(
    @Size(max = 255, message = "菜单内容最多255字符")
    String mon,
    @Size(max = 255, message = "菜单内容最多255字符")
    String tue,
    @Size(max = 255, message = "菜单内容最多255字符")
    String wed,
    @Size(max = 255, message = "菜单内容最多255字符")
    String thu,
    @Size(max = 255, message = "菜单内容最多255字符")
    String fri,
    @Size(max = 255, message = "菜单内容最多255字符")
    String sat,
    @Size(max = 255, message = "菜单内容最多255字符")
    String sun
) {
}
