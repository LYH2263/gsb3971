package com.hanyu.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.domain.entity.CustomerEntity;
import com.hanyu.learning.domain.entity.CustomerMealPlanEntity;
import com.hanyu.learning.domain.entity.MealWeeklyMenuEntity;
import com.hanyu.learning.dto.request.SaveCustomerMealPlanRequest;
import com.hanyu.learning.dto.request.SaveWeeklyMenuRequest;
import com.hanyu.learning.dto.view.CustomerMealPlanView;
import com.hanyu.learning.dto.view.WeeklyMenuView;
import com.hanyu.learning.mapper.CustomerMapper;
import com.hanyu.learning.mapper.CustomerMealPlanMapper;
import com.hanyu.learning.mapper.MealWeeklyMenuMapper;
import com.hanyu.learning.security.AuthUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MealService {

    private final MealWeeklyMenuMapper mealWeeklyMenuMapper;
    private final CustomerMealPlanMapper customerMealPlanMapper;
    private final CustomerMapper customerMapper;
    private final PermissionService permissionService;

    public MealService(
        MealWeeklyMenuMapper mealWeeklyMenuMapper,
        CustomerMealPlanMapper customerMealPlanMapper,
        CustomerMapper customerMapper,
        PermissionService permissionService
    ) {
        this.mealWeeklyMenuMapper = mealWeeklyMenuMapper;
        this.customerMealPlanMapper = customerMealPlanMapper;
        this.customerMapper = customerMapper;
        this.permissionService = permissionService;
    }

    public WeeklyMenuView saveWeeklyMenu(LocalDate weekStartDate, SaveWeeklyMenuRequest request) {
        permissionService.requireLogin();
        MealWeeklyMenuEntity entity = mealWeeklyMenuMapper.selectOne(
            new LambdaQueryWrapper<MealWeeklyMenuEntity>()
                .eq(MealWeeklyMenuEntity::getWeekStartDate, weekStartDate)
                .last("limit 1")
        );

        if (entity == null) {
            entity = new MealWeeklyMenuEntity();
            entity.setWeekStartDate(weekStartDate);
            applyWeeklyMenu(request, entity);
            entity.setUpdatedAt(LocalDateTime.now());
            mealWeeklyMenuMapper.insert(entity);
        } else {
            applyWeeklyMenu(request, entity);
            entity.setUpdatedAt(LocalDateTime.now());
            mealWeeklyMenuMapper.updateById(entity);
        }
        return toWeeklyMenuView(entity);
    }

    public WeeklyMenuView getWeeklyMenu(LocalDate weekStartDate) {
        permissionService.requireLogin();
        MealWeeklyMenuEntity entity = mealWeeklyMenuMapper.selectOne(
            new LambdaQueryWrapper<MealWeeklyMenuEntity>()
                .eq(MealWeeklyMenuEntity::getWeekStartDate, weekStartDate)
                .last("limit 1")
        );
        if (entity == null) {
            return new WeeklyMenuView(weekStartDate, "", "", "", "", "", "", "");
        }
        return toWeeklyMenuView(entity);
    }

    public CustomerMealPlanView saveCustomerMealPlan(
        Long customerId,
        LocalDate weekStartDate,
        SaveCustomerMealPlanRequest request
    ) {
        AuthUser loginUser = permissionService.requireLogin();
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "学员不存在");
        }

        CustomerMealPlanEntity entity = customerMealPlanMapper.selectOne(
            new LambdaQueryWrapper<CustomerMealPlanEntity>()
                .eq(CustomerMealPlanEntity::getCustomerId, customerId)
                .eq(CustomerMealPlanEntity::getWeekStartDate, weekStartDate)
                .last("limit 1")
        );

        if (entity == null) {
            entity = new CustomerMealPlanEntity();
            entity.setCustomerId(customerId);
            entity.setWeekStartDate(weekStartDate);
            entity.setCreatedBy(loginUser.id());
            applyPlan(request, entity);
            customerMealPlanMapper.insert(entity);
        } else {
            applyPlan(request, entity);
            entity.setCreatedBy(loginUser.id());
            customerMealPlanMapper.updateById(entity);
        }

        return new CustomerMealPlanView(
            entity.getId(),
            entity.getCustomerId(),
            customer.getName(),
            entity.getWeekStartDate(),
            entity.getMealType(),
            entity.getDietTaboo(),
            entity.getNote(),
            entity.getCreatedBy()
        );
    }

    public CustomerMealPlanView getCustomerMealPlan(Long customerId, LocalDate weekStartDate) {
        permissionService.requireLogin();
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "学员不存在");
        }
        CustomerMealPlanEntity entity = customerMealPlanMapper.selectOne(
            new LambdaQueryWrapper<CustomerMealPlanEntity>()
                .eq(CustomerMealPlanEntity::getCustomerId, customerId)
                .eq(CustomerMealPlanEntity::getWeekStartDate, weekStartDate)
                .last("limit 1")
        );
        if (entity == null) {
            return new CustomerMealPlanView(null, customerId, customer.getName(), weekStartDate, "", "", "", null);
        }
        return new CustomerMealPlanView(
            entity.getId(),
            entity.getCustomerId(),
            customer.getName(),
            entity.getWeekStartDate(),
            entity.getMealType(),
            entity.getDietTaboo(),
            entity.getNote(),
            entity.getCreatedBy()
        );
    }

    public List<CustomerMealPlanView> listCustomerPlans(LocalDate weekStartDate, Long customerId) {
        permissionService.requireLogin();
        LambdaQueryWrapper<CustomerMealPlanEntity> query = new LambdaQueryWrapper<CustomerMealPlanEntity>()
            .orderByDesc(CustomerMealPlanEntity::getId);
        if (weekStartDate != null) {
            query.eq(CustomerMealPlanEntity::getWeekStartDate, weekStartDate);
        }
        if (customerId != null) {
            query.eq(CustomerMealPlanEntity::getCustomerId, customerId);
        }

        List<CustomerMealPlanEntity> entities = customerMealPlanMapper.selectList(query);
        List<Long> customerIds = entities.stream().map(CustomerMealPlanEntity::getCustomerId).distinct().toList();
        Map<Long, String> customerNameMap = customerIds.isEmpty() ? Map.of() : customerMapper.selectBatchIds(customerIds).stream()
            .collect(Collectors.toMap(CustomerEntity::getId, CustomerEntity::getName));

        return entities.stream().map(entity -> new CustomerMealPlanView(
            entity.getId(),
            entity.getCustomerId(),
            customerNameMap.get(entity.getCustomerId()),
            entity.getWeekStartDate(),
            entity.getMealType(),
            entity.getDietTaboo(),
            entity.getNote(),
            entity.getCreatedBy()
        )).toList();
    }

    private void applyWeeklyMenu(SaveWeeklyMenuRequest request, MealWeeklyMenuEntity entity) {
        entity.setMon(request.mon());
        entity.setTue(request.tue());
        entity.setWed(request.wed());
        entity.setThu(request.thu());
        entity.setFri(request.fri());
        entity.setSat(request.sat());
        entity.setSun(request.sun());
    }

    private void applyPlan(SaveCustomerMealPlanRequest request, CustomerMealPlanEntity entity) {
        entity.setMealType(request.mealType());
        entity.setDietTaboo(request.dietTaboo());
        entity.setNote(request.note());
    }

    private WeeklyMenuView toWeeklyMenuView(MealWeeklyMenuEntity entity) {
        return new WeeklyMenuView(
            entity.getWeekStartDate(),
            entity.getMon(),
            entity.getTue(),
            entity.getWed(),
            entity.getThu(),
            entity.getFri(),
            entity.getSat(),
            entity.getSun()
        );
    }
}
