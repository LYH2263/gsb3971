package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import com.hanyu.learning.dto.request.AssignServiceObjectRequest;
import com.hanyu.learning.dto.request.CreateServiceFocusRequest;
import com.hanyu.learning.dto.view.ServiceFocusView;
import com.hanyu.learning.dto.view.ServiceObjectView;
import com.hanyu.learning.service.CustomerServiceFocusService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class CustomerServiceFocusController {

    private final CustomerServiceFocusService customerServiceFocusService;

    public CustomerServiceFocusController(CustomerServiceFocusService customerServiceFocusService) {
        this.customerServiceFocusService = customerServiceFocusService;
    }

    @GetMapping("/objects")
    public ApiResponse<List<ServiceObjectView>> listServiceObjects(@RequestParam(required = false) Long customerId) {
        return ApiResponse.success(customerServiceFocusService.listServiceObjects(customerId));
    }

    @PutMapping("/objects/{customerId}")
    public ApiResponse<ServiceObjectView> assignServiceObject(
        @PathVariable Long customerId,
        @Valid @RequestBody AssignServiceObjectRequest request
    ) {
        return ApiResponse.success(customerServiceFocusService.assignServiceObject(customerId, request));
    }

    @GetMapping("/focuses")
    public ApiResponse<List<ServiceFocusView>> listServiceFocuses(@RequestParam(required = false) Long customerId) {
        return ApiResponse.success(customerServiceFocusService.listServiceFocuses(customerId));
    }

    @PostMapping("/focuses")
    public ApiResponse<ServiceFocusView> createServiceFocus(@Valid @RequestBody CreateServiceFocusRequest request) {
        return ApiResponse.success(customerServiceFocusService.createServiceFocus(request));
    }
}

