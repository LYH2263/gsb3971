package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import com.hanyu.learning.dto.request.CreateCustomerRequest;
import com.hanyu.learning.dto.request.CustomerLifecycleRequest;
import com.hanyu.learning.dto.view.CustomerView;
import com.hanyu.learning.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ApiResponse<List<CustomerView>> listCustomers(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(customerService.listCustomers(status, keyword));
    }

    @PostMapping
    public ApiResponse<CustomerView> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.success(customerService.createCustomer(request));
    }

    @PatchMapping("/{id}/lifecycle")
    public ApiResponse<CustomerView> updateLifecycle(
        @PathVariable Long id,
        @Valid @RequestBody CustomerLifecycleRequest request
    ) {
        return ApiResponse.success(customerService.applyLifecycle(id, request));
    }
}
