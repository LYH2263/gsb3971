package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import com.hanyu.learning.dto.request.CreateCareLevelRequest;
import com.hanyu.learning.dto.request.CreateCareRecordRequest;
import com.hanyu.learning.dto.request.UpdateCareLevelStatusRequest;
import com.hanyu.learning.dto.view.CareLevelView;
import com.hanyu.learning.dto.view.CareRecordView;
import com.hanyu.learning.service.CareService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CareController {

    private final CareService careService;

    public CareController(CareService careService) {
        this.careService = careService;
    }

    @GetMapping("/care-levels")
    public ApiResponse<List<CareLevelView>> listCareLevels() {
        return ApiResponse.success(careService.listCareLevels());
    }

    @PostMapping("/care-levels")
    public ApiResponse<CareLevelView> createCareLevel(@Valid @RequestBody CreateCareLevelRequest request) {
        return ApiResponse.success(careService.createCareLevel(request));
    }

    @PatchMapping("/care-levels/{id}/status")
    public ApiResponse<CareLevelView> updateCareLevelStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCareLevelStatusRequest request
    ) {
        return ApiResponse.success(careService.updateCareLevelStatus(id, request.status()));
    }

    @GetMapping("/care-records")
    public ApiResponse<List<CareRecordView>> listCareRecords(
        @RequestParam(required = false) Long customerId,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to
    ) {
        return ApiResponse.success(careService.listCareRecords(customerId, from, to));
    }

    @PostMapping("/care-records")
    public ApiResponse<CareRecordView> createCareRecord(@Valid @RequestBody CreateCareRecordRequest request) {
        return ApiResponse.success(careService.createCareRecord(request));
    }
}
