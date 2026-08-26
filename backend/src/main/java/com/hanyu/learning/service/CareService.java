package com.hanyu.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.domain.entity.CareLevelEntity;
import com.hanyu.learning.domain.entity.CareRecordEntity;
import com.hanyu.learning.domain.entity.CustomerEntity;
import com.hanyu.learning.domain.entity.UserEntity;
import com.hanyu.learning.dto.request.CreateCareLevelRequest;
import com.hanyu.learning.dto.request.CreateCareRecordRequest;
import com.hanyu.learning.dto.view.CareLevelView;
import com.hanyu.learning.dto.view.CareRecordView;
import com.hanyu.learning.mapper.CareLevelMapper;
import com.hanyu.learning.mapper.CareRecordMapper;
import com.hanyu.learning.mapper.CustomerMapper;
import com.hanyu.learning.mapper.UserMapper;
import com.hanyu.learning.security.AuthUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CareService {

    private final CareLevelMapper careLevelMapper;
    private final CareRecordMapper careRecordMapper;
    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final PermissionService permissionService;

    public CareService(
        CareLevelMapper careLevelMapper,
        CareRecordMapper careRecordMapper,
        CustomerMapper customerMapper,
        UserMapper userMapper,
        PermissionService permissionService
    ) {
        this.careLevelMapper = careLevelMapper;
        this.careRecordMapper = careRecordMapper;
        this.customerMapper = customerMapper;
        this.userMapper = userMapper;
        this.permissionService = permissionService;
    }

    public List<CareLevelView> listCareLevels() {
        permissionService.requireLogin();
        return careLevelMapper.selectList(new LambdaQueryWrapper<CareLevelEntity>().orderByDesc(CareLevelEntity::getId))
            .stream().map(level -> new CareLevelView(level.getId(), level.getName(), level.getDescription(), level.getStatus()))
            .toList();
    }

    public CareLevelView createCareLevel(CreateCareLevelRequest request) {
        permissionService.requireLogin();
        CareLevelEntity entity = new CareLevelEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setStatus(request.status() == null ? 1 : request.status());
        careLevelMapper.insert(entity);
        return new CareLevelView(entity.getId(), entity.getName(), entity.getDescription(), entity.getStatus());
    }

    public CareLevelView updateCareLevelStatus(Long id, Integer status) {
        permissionService.requireLogin();
        CareLevelEntity entity = careLevelMapper.selectById(id);
        if (entity == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "带教档位不存在");
        }
        entity.setStatus(status);
        careLevelMapper.updateById(entity);
        return new CareLevelView(entity.getId(), entity.getName(), entity.getDescription(), entity.getStatus());
    }

    public CareRecordView createCareRecord(CreateCareRecordRequest request) {
        AuthUser currentUser = permissionService.requireLogin();
        CustomerEntity customer = customerMapper.selectById(request.customerId());
        if (customer == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "学员不存在");
        }

        CareRecordEntity entity = new CareRecordEntity();
        entity.setCustomerId(request.customerId());
        entity.setCareDate(request.careDate());
        entity.setContent(request.content());
        entity.setPerformedBy(currentUser.id());
        careRecordMapper.insert(entity);

        return new CareRecordView(
            entity.getId(),
            customer.getId(),
            customer.getName(),
            entity.getCareDate(),
            entity.getContent(),
            currentUser.id(),
            currentUser.realName()
        );
    }

    public List<CareRecordView> listCareRecords(Long customerId, LocalDateTime from, LocalDateTime to) {
        permissionService.requireLogin();
        LambdaQueryWrapper<CareRecordEntity> query = new LambdaQueryWrapper<CareRecordEntity>()
            .orderByDesc(CareRecordEntity::getCareDate);
        if (customerId != null) {
            query.eq(CareRecordEntity::getCustomerId, customerId);
        }
        if (from != null) {
            query.ge(CareRecordEntity::getCareDate, from);
        }
        if (to != null) {
            query.le(CareRecordEntity::getCareDate, to);
        }

        List<CareRecordEntity> records = careRecordMapper.selectList(query);
        List<Long> customerIds = records.stream().map(CareRecordEntity::getCustomerId).distinct().toList();
        List<Long> performerIds = records.stream().map(CareRecordEntity::getPerformedBy).distinct().toList();

        Map<Long, String> customerNameMap = customerIds.isEmpty() ? Map.of() : customerMapper.selectBatchIds(customerIds).stream()
            .collect(Collectors.toMap(CustomerEntity::getId, CustomerEntity::getName));
        Map<Long, String> performerNameMap = performerIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(performerIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, UserEntity::getRealName));

        return records.stream().map(record -> new CareRecordView(
            record.getId(),
            record.getCustomerId(),
            customerNameMap.get(record.getCustomerId()),
            record.getCareDate(),
            record.getContent(),
            record.getPerformedBy(),
            performerNameMap.get(record.getPerformedBy())
        )).toList();
    }
}
