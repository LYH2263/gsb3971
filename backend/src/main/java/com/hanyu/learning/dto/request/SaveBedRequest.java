package com.hanyu.learning.dto.request;

import jakarta.validation.constraints.Size;

public record SaveBedRequest(
    Long bedId,
    @Size(max = 20, message = "铺位号最多20字符")
    String bedNo,
    String status
) {
}
