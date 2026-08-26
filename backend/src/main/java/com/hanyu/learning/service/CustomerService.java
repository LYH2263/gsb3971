package com.hanyu.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.constant.BedStatus;
import com.hanyu.learning.constant.CustomerStatus;
import com.hanyu.learning.domain.entity.BedEntity;
import com.hanyu.learning.domain.entity.CustomerEntity;
import com.hanyu.learning.domain.entity.RoomEntity;
import com.hanyu.learning.dto.request.CreateCustomerRequest;
import com.hanyu.learning.dto.request.CustomerLifecycleRequest;
import com.hanyu.learning.dto.view.CustomerView;
import com.hanyu.learning.mapper.BedMapper;
import com.hanyu.learning.mapper.CustomerMapper;
import com.hanyu.learning.mapper.RoomMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final PermissionService permissionService;

    public CustomerService(
        CustomerMapper customerMapper,
        BedMapper bedMapper,
        RoomMapper roomMapper,
        PermissionService permissionService
    ) {
        this.customerMapper = customerMapper;
        this.bedMapper = bedMapper;
        this.roomMapper = roomMapper;
        this.permissionService = permissionService;
    }

    public List<CustomerView> listCustomers(String status, String keyword) {
        permissionService.requireLogin();
        LambdaQueryWrapper<CustomerEntity> query = new LambdaQueryWrapper<CustomerEntity>()
            .orderByDesc(CustomerEntity::getId);
        if (StringUtils.hasText(status)) {
            query.eq(CustomerEntity::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                .like(CustomerEntity::getName, keyword)
                .or()
                .like(CustomerEntity::getPhone, keyword));
        }

        List<CustomerEntity> customers = customerMapper.selectList(query);
        List<Long> bedIds = customers.stream().map(CustomerEntity::getBedId).filter(Objects::nonNull).distinct().toList();
        Map<Long, BedEntity> bedMap = bedIds.isEmpty() ? Map.of() : bedMapper.selectBatchIds(bedIds).stream()
            .collect(Collectors.toMap(BedEntity::getId, bed -> bed));
        List<Long> roomIds = bedMap.values().stream().map(BedEntity::getRoomId).distinct().toList();
        Map<Long, RoomEntity> roomMap = roomIds.isEmpty() ? Map.of() : roomMapper.selectBatchIds(roomIds).stream()
            .collect(Collectors.toMap(RoomEntity::getId, room -> room));

        return customers.stream().map(customer -> {
            BedEntity bed = customer.getBedId() == null ? null : bedMap.get(customer.getBedId());
            RoomEntity room = bed == null ? null : roomMap.get(bed.getRoomId());
            return new CustomerView(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getAge(),
                customer.getGender(),
                customer.getStatus(),
                customer.getBedId(),
                bed == null ? null : bed.getBedNo(),
                room == null ? null : room.getRoomNo(),
                customer.getCheckinDate(),
                customer.getNote()
            );
        }).toList();
    }

    public CustomerView createCustomer(CreateCustomerRequest request) {
        permissionService.requireLogin();
        CustomerEntity entity = new CustomerEntity();
        entity.setName(request.name());
        entity.setPhone(request.phone());
        entity.setAge(request.age());
        entity.setGender(request.gender());
        entity.setStatus(CustomerStatus.DRAFT);
        entity.setNote(request.note());
        entity.setCreatedAt(LocalDateTime.now());
        customerMapper.insert(entity);
        return new CustomerView(
            entity.getId(),
            entity.getName(),
            entity.getPhone(),
            entity.getAge(),
            entity.getGender(),
            entity.getStatus(),
            null,
            null,
            null,
            null,
            entity.getNote()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public CustomerView applyLifecycle(Long customerId, CustomerLifecycleRequest request) {
        permissionService.requireLogin();
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "学员不存在");
        }

        String action = request.action().toLowerCase(Locale.ROOT);
        return switch (action) {
            case "checkin" -> checkin(customer, request);
            case "discharge" -> discharge(customer);
            case "outing" -> outing(customer);
            default -> throw new AppException(HttpStatus.BAD_REQUEST.value(), ErrorCodes.VALIDATION_ERROR, "无效的生命周期动作");
        };
    }

    private CustomerView checkin(CustomerEntity customer, CustomerLifecycleRequest request) {
        if (request.bedId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), ErrorCodes.VALIDATION_ERROR, "入营必须选择铺位");
        }
        BedEntity targetBed = bedMapper.selectById(request.bedId());
        if (targetBed == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "铺位不存在");
        }
        if (BedStatus.DISABLED.equals(targetBed.getStatus())) {
            throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BUSINESS_CONFLICT, "铺位已停用");
        }

        long occupiedByOthers = customerMapper.selectCount(new LambdaQueryWrapper<CustomerEntity>()
            .eq(CustomerEntity::getBedId, targetBed.getId())
            .in(CustomerEntity::getStatus, Arrays.asList(CustomerStatus.RESIDENT, CustomerStatus.OUTING))
            .ne(CustomerEntity::getId, customer.getId()));
        if (occupiedByOthers > 0) {
            throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BED_OCCUPIED, "铺位已占用");
        }

        Long oldBedId = customer.getBedId();
        if (oldBedId != null && !Objects.equals(oldBedId, targetBed.getId())) {
            BedEntity oldBed = bedMapper.selectById(oldBedId);
            if (oldBed != null && !BedStatus.DISABLED.equals(oldBed.getStatus())) {
                oldBed.setStatus(BedStatus.AVAILABLE);
                bedMapper.updateById(oldBed);
            }
        }

        targetBed.setStatus(BedStatus.OCCUPIED);
        bedMapper.updateById(targetBed);

        customer.setStatus(CustomerStatus.RESIDENT);
        customer.setBedId(targetBed.getId());
        customer.setCheckinDate(request.actionDate());
        customerMapper.updateById(customer);

        return buildView(customer, targetBed);
    }

    private CustomerView discharge(CustomerEntity customer) {
        if (!Set.of(CustomerStatus.RESIDENT, CustomerStatus.OUTING).contains(customer.getStatus())) {
            throw new AppException(
                HttpStatus.CONFLICT.value(),
                ErrorCodes.CUSTOMER_STATUS_INVALID,
                "当前状态不允许结营"
            );
        }
        BedEntity currentBed = customer.getBedId() == null ? null : bedMapper.selectById(customer.getBedId());
        if (currentBed != null && !BedStatus.DISABLED.equals(currentBed.getStatus())) {
            currentBed.setStatus(BedStatus.AVAILABLE);
            bedMapper.updateById(currentBed);
        }

        customer.setStatus(CustomerStatus.DISCHARGED);
        customer.setBedId(null);
        customerMapper.updateById(customer);
        return buildView(customer, null);
    }

    private CustomerView outing(CustomerEntity customer) {
        if (!CustomerStatus.RESIDENT.equals(customer.getStatus())) {
            throw new AppException(
                HttpStatus.CONFLICT.value(),
                ErrorCodes.CUSTOMER_STATUS_INVALID,
                "状态不允许离营外出，仅在营学员可外出"
            );
        }
        customer.setStatus(CustomerStatus.OUTING);
        customerMapper.updateById(customer);
        BedEntity bed = customer.getBedId() == null ? null : bedMapper.selectById(customer.getBedId());
        return buildView(customer, bed);
    }

    private CustomerView buildView(CustomerEntity customer, BedEntity bed) {
        RoomEntity room = null;
        if (bed != null) {
            room = roomMapper.selectById(bed.getRoomId());
        }
        return new CustomerView(
            customer.getId(),
            customer.getName(),
            customer.getPhone(),
            customer.getAge(),
            customer.getGender(),
            customer.getStatus(),
            customer.getBedId(),
            bed == null ? null : bed.getBedNo(),
            room == null ? null : room.getRoomNo(),
            customer.getCheckinDate(),
            customer.getNote()
        );
    }
}
