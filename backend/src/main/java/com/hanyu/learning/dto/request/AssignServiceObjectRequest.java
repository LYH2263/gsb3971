package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignServiceObjectRequest(
    @NotNull(message = "带队导师ID不能为空")
    Long managerUserId
) {
}

