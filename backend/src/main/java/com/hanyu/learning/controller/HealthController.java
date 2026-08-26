package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss"
    );

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(
            Map.of("status", "UP", "time", LocalDateTime.now().format(DEFAULT_DATE_TIME_FORMATTER))
        );
    }
}
