package com.hanyu.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.constant.RoleConstants;
import com.hanyu.learning.domain.entity.CustomerEntity;
import com.hanyu.learning.domain.entity.CustomerServiceFocusEntity;
import com.hanyu.learning.domain.entity.CustomerServiceObjectEntity;
import com.hanyu.learning.domain.entity.UserEntity;
import com.hanyu.learning.dto.request.AssignServiceObjectRequest;
import com.hanyu.learning.dto.request.CreateServiceFocusRequest;
import com.hanyu.learning.dto.view.ServiceFocusView;
import com.hanyu.learning.dto.view.ServiceObjectView;
import com.hanyu.learning.security.AuthUser;
import com.hanyu.learning.mapper.CustomerMapper;
import com.hanyu.learning.mapper.CustomerServiceFocusMapper;
import com.hanyu.learning.mapper.CustomerServiceObjectMapper;
import com.hanyu.learning.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceFocusService {

    private static final Set<String> ALLOWED_FOCUS_STATUS = Set.of("ACTIVE", "PAUSED", "ENDED");

    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final CustomerServiceObjectMapper customerServiceObjectMapper;
    private final CustomerServiceFocusMapper customerServiceFocusMapper;
    private final PermissionService permissionService;

    public CustomerServiceFocusService(
        CustomerMapper customerMapper,
        UserMapper userMapper,
        CustomerServiceObjectMapper customerServiceObjectMapper,
        CustomerServiceFocusMapper customerServiceFocusMapper,
        PermissionService permissionService
    ) {
        this.customerMapper = customerMapper;
        this.userMapper = userMapper;
        this.customerServiceObjectMapper = customerServiceObjectMapper;
        this.customerServiceFocusMapper = customerServiceFocusMapper;
        this.permissionService = permissionService;
    }

    public List<ServiceObjectView> listServiceObjects(Long customerId) {
        permissionService.requireLogin();

        LambdaQueryWrapper<CustomerEntity> customerQuery = new LambdaQueryWrapper<CustomerEntity>()
            .orderByDesc(CustomerEntity::getId);
        if (customerId != null) {
            customerQuery.eq(CustomerEntity::getId, customerId);
        }
        List<CustomerEntity> customers = customerMapper.selectList(customerQuery);
        if (customers.isEmpty()) {
            return List.of();
        }

        List<Long> customerIds = customers.stream().map(CustomerEntity::getId).toList();
        List<CustomerServiceObjectEntity> relations = customerServiceObjectMapper.selectList(
            new LambdaQueryWrapper<CustomerServiceObjectEntity>().in(CustomerServiceObjectEntity::getCustomerId, customerIds)
        );
        Map<Long, CustomerServiceObjectEntity> relationMap = relations.stream()
            .collect(Collectors.toMap(CustomerServiceObjectEntity::getCustomerId, relation -> relation));

        List<Long> managerIds = relations.stream().map(CustomerServiceObjectEntity::getManagerUserId).distinct().toList();
        Map<Long, UserEntity> managerMap = managerIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(managerIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, user -> user));

        return customers.stream().map(customer -> {
            CustomerServiceObjectEntity relation = relationMap.get(customer.getId());
            UserEntity manager = relation == null ? null : managerMap.get(relation.getManagerUserId());
            return new ServiceObjectView(
                customer.getId(),
                customer.getName(),
                relation == null ? null : relation.getManagerUserId(),
                manager == null ? null : manager.getRealName(),
                manager == null ? null : manager.getPhone(),
                relation == null ? null : relation.getAssignedAt()
            );
        }).toList();
    }

    public ServiceObjectView assignServiceObject(Long customerId, AssignServiceObjectRequest request) {
        permissionService.requireAdmin();

        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "学员不存在");
        }

        UserEntity manager = userMapper.selectById(request.managerUserId());
        if (manager == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "带队导师不存在");
        }
        if (!RoleConstants.STAFF.equals(manager.getRole())) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), ErrorCodes.VALIDATION_ERROR, "带队导师必须是营务员工角色");
        }
        if (!Objects.equals(manager.getStatus(), 1)) {
            throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BUSINESS_CONFLICT, "带队导师账号已停用");
        }

        LocalDateTime now = LocalDateTime.now();
        CustomerServiceObjectEntity relation = customerServiceObjectMapper.selectOne(
            new LambdaQueryWrapper<CustomerServiceObjectEntity>()
                .eq(CustomerServiceObjectEntity::getCustomerId, customerId)
                .last("limit 1")
        );
        if (relation == null) {
            relation = new CustomerServiceObjectEntity();
            relation.setCustomerId(customerId);
            relation.setManagerUserId(manager.getId());
            relation.setAssignedAt(now);
            customerServiceObjectMapper.insert(relation);
        } else {
            relation.setManagerUserId(manager.getId());
            relation.setAssignedAt(now);
            customerServiceObjectMapper.updateById(relation);
        }

        return new ServiceObjectView(
            customer.getId(),
            customer.getName(),
            manager.getId(),
            manager.getRealName(),
            manager.getPhone(),
            relation.getAssignedAt()
        );
    }

    public List<ServiceFocusView> listServiceFocuses(Long customerId) {
        permissionService.requireLogin();

        LambdaQueryWrapper<CustomerServiceFocusEntity> query = new LambdaQueryWrapper<CustomerServiceFocusEntity>()
            .orderByDesc(CustomerServiceFocusEntity::getId);
        if (customerId != null) {
            query.eq(CustomerServiceFocusEntity::getCustomerId, customerId);
        }
        List<CustomerServiceFocusEntity> entities = customerServiceFocusMapper.selectList(query);
        if (entities.isEmpty()) {
            return List.of();
        }

        List<Long> customerIds = entities.stream().map(CustomerServiceFocusEntity::getCustomerId).distinct().toList();
        Map<Long, String> customerNameMap = customerMapper.selectBatchIds(customerIds).stream()
            .collect(Collectors.toMap(CustomerEntity::getId, CustomerEntity::getName));

        List<Long> creatorIds = entities.stream().map(CustomerServiceFocusEntity::getCreatedBy).distinct().toList();
        Map<Long, String> creatorNameMap = userMapper.selectBatchIds(creatorIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName));

        return entities.stream().map(entity -> new ServiceFocusView(
            entity.getId(),
            entity.getCustomerId(),
            customerNameMap.get(entity.getCustomerId()),
            entity.getServiceName(),
            entity.getPurchaseDate(),
            entity.getExpireDate(),
            entity.getServiceStatus(),
            entity.getNote(),
            entity.getCreatedBy(),
            creatorNameMap.get(entity.getCreatedBy()),
            entity.getCreatedAt()
        )).toList();
    }

    public ServiceFocusView createServiceFocus(CreateServiceFocusRequest request) {
        AuthUser currentUser = permissionService.requireLogin();

        CustomerEntity customer = customerMapper.selectById(request.customerId());
        if (customer == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "学员不存在");
        }

        if (request.expireDate() != null && request.expireDate().isBefore(request.purchaseDate())) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), ErrorCodes.VALIDATION_ERROR, "到期日期不能早于报名日期");
        }

        String normalizedStatus = request.serviceStatus().toUpperCase(Locale.ROOT);
        if (!ALLOWED_FOCUS_STATUS.contains(normalizedStatus)) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), ErrorCodes.VALIDATION_ERROR, "服务状态取值非法");
        }

        CustomerServiceFocusEntity entity = new CustomerServiceFocusEntity();
        entity.setCustomerId(request.customerId());
        entity.setServiceName(request.serviceName());
        entity.setPurchaseDate(request.purchaseDate());
        entity.setExpireDate(request.expireDate());
        entity.setServiceStatus(normalizedStatus);
        entity.setNote(request.note());
        entity.setCreatedBy(currentUser.id());
        entity.setCreatedAt(LocalDateTime.now());
        customerServiceFocusMapper.insert(entity);

        return new ServiceFocusView(
            entity.getId(),
            entity.getCustomerId(),
            customer.getName(),
            entity.getServiceName(),
            entity.getPurchaseDate(),
            entity.getExpireDate(),
            entity.getServiceStatus(),
            entity.getNote(),
            entity.getCreatedBy(),
            currentUser.realName(),
            entity.getCreatedAt()
        );
    }
}

