package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import com.hanyu.learning.dto.request.SaveCustomerMealPlanRequest;
import com.hanyu.learning.dto.request.SaveWeeklyMenuRequest;
import com.hanyu.learning.dto.view.CustomerMealPlanView;
import com.hanyu.learning.dto.view.WeeklyMenuView;
import com.hanyu.learning.service.MealService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PutMapping("/weekly-menus/{weekStartDate}")
    public ApiResponse<WeeklyMenuView> saveWeeklyMenu(
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStartDate,
        @Valid @RequestBody SaveWeeklyMenuRequest request
    ) {
        return ApiResponse.success(mealService.saveWeeklyMenu(weekStartDate, request));
    }

    @GetMapping("/weekly-menus/{weekStartDate}")
    public ApiResponse<WeeklyMenuView> getWeeklyMenu(
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStartDate
    ) {
        return ApiResponse.success(mealService.getWeeklyMenu(weekStartDate));
    }

    @PutMapping("/customer-plans/{customerId}/{weekStartDate}")
    public ApiResponse<CustomerMealPlanView> saveCustomerPlan(
        @PathVariable Long customerId,
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStartDate,
        @Valid @RequestBody SaveCustomerMealPlanRequest request
    ) {
        return ApiResponse.success(mealService.saveCustomerMealPlan(customerId, weekStartDate, request));
    }

    @GetMapping("/customer-plans/{customerId}/{weekStartDate}")
    public ApiResponse<CustomerMealPlanView> getCustomerPlan(
        @PathVariable Long customerId,
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStartDate
    ) {
        return ApiResponse.success(mealService.getCustomerMealPlan(customerId, weekStartDate));
    }

    @GetMapping("/customer-plans")
    public ApiResponse<List<CustomerMealPlanView>> listCustomerPlans(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStartDate,
        @RequestParam(required = false) Long customerId
    ) {
        return ApiResponse.success(mealService.listCustomerPlans(weekStartDate, customerId));
    }
}
