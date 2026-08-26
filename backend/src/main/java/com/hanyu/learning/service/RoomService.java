package com.hanyu.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hanyu.learning.common.exception.AppException;
import com.hanyu.learning.common.exception.ErrorCodes;
import com.hanyu.learning.constant.BedStatus;
import com.hanyu.learning.constant.CustomerStatus;
import com.hanyu.learning.domain.entity.BedEntity;
import com.hanyu.learning.domain.entity.CustomerEntity;
import com.hanyu.learning.domain.entity.RoomEntity;
import com.hanyu.learning.dto.request.SaveBedRequest;
import com.hanyu.learning.dto.request.SaveRoomRequest;
import com.hanyu.learning.dto.view.BedView;
import com.hanyu.learning.dto.view.RoomView;
import com.hanyu.learning.mapper.BedMapper;
import com.hanyu.learning.mapper.CustomerMapper;
import com.hanyu.learning.mapper.RoomMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final CustomerMapper customerMapper;
    private final PermissionService permissionService;

    public RoomService(
        RoomMapper roomMapper,
        BedMapper bedMapper,
        CustomerMapper customerMapper,
        PermissionService permissionService
    ) {
        this.roomMapper = roomMapper;
        this.bedMapper = bedMapper;
        this.customerMapper = customerMapper;
        this.permissionService = permissionService;
    }

    public List<RoomView> listRooms(boolean includeBeds) {
        permissionService.requireLogin();
        List<RoomEntity> rooms = roomMapper.selectList(
            new LambdaQueryWrapper<RoomEntity>()
                .orderByAsc(RoomEntity::getFloor)
                .orderByAsc(RoomEntity::getRoomNo)
        );
        if (!includeBeds) {
            return rooms.stream()
                .map(room -> new RoomView(room.getId(), room.getFloor(), room.getRoomNo(), room.getStatus(), Collections.emptyList()))
                .toList();
        }

        List<BedEntity> beds = bedMapper.selectList(new LambdaQueryWrapper<BedEntity>().orderByAsc(BedEntity::getRoomId, BedEntity::getBedNo));
        Map<Long, RoomEntity> roomMap = rooms.stream().collect(Collectors.toMap(RoomEntity::getId, room -> room));

        List<CustomerEntity> occupiedCustomers = customerMapper.selectList(
            new LambdaQueryWrapper<CustomerEntity>()
                .isNotNull(CustomerEntity::getBedId)
                .in(CustomerEntity::getStatus, CustomerStatus.RESIDENT, CustomerStatus.OUTING)
        );
        Map<Long, CustomerEntity> bedCustomerMap = occupiedCustomers.stream()
            .collect(Collectors.toMap(CustomerEntity::getBedId, customer -> customer, (a, b) -> a));

        Map<Long, List<BedView>> roomBeds = beds.stream().map(bed -> {
            CustomerEntity customer = bedCustomerMap.get(bed.getId());
            RoomEntity room = roomMap.get(bed.getRoomId());
            return new BedView(
                bed.getId(),
                bed.getRoomId(),
                room == null ? "" : room.getRoomNo(),
                bed.getBedNo(),
                bed.getStatus(),
                customer == null ? null : customer.getId(),
                customer == null ? null : customer.getName()
            );
        }).collect(Collectors.groupingBy(BedView::roomId));

        return rooms.stream()
            .map(room -> new RoomView(
                room.getId(),
                room.getFloor(),
                room.getRoomNo(),
                room.getStatus(),
                roomBeds.getOrDefault(room.getId(), Collections.emptyList())
            ))
            .toList();
    }

    public RoomView saveRoom(SaveRoomRequest request) {
        permissionService.requireAdmin();
        RoomEntity duplicate = roomMapper.selectOne(new LambdaQueryWrapper<RoomEntity>()
            .eq(RoomEntity::getFloor, request.floor())
            .eq(RoomEntity::getRoomNo, request.roomNo())
            .last("limit 1"));

        if (request.id() == null && duplicate != null) {
            throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BUSINESS_CONFLICT, "房间号已存在");
        }
        if (request.id() != null && duplicate != null && !Objects.equals(duplicate.getId(), request.id())) {
            throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BUSINESS_CONFLICT, "房间号已存在");
        }

        RoomEntity room;
        if (request.id() == null) {
            room = new RoomEntity();
        } else {
            room = roomMapper.selectById(request.id());
            if (room == null) {
                throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "房间不存在");
            }
        }

        room.setFloor(request.floor());
        room.setRoomNo(request.roomNo());
        room.setStatus(request.status() == null ? 1 : request.status());

        if (request.id() == null) {
            roomMapper.insert(room);
        } else {
            roomMapper.updateById(room);
        }
        return new RoomView(room.getId(), room.getFloor(), room.getRoomNo(), room.getStatus(), Collections.emptyList());
    }

    public BedView saveBed(Long roomId, SaveBedRequest request) {
        permissionService.requireAdmin();
        RoomEntity room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "房间不存在");
        }

        BedEntity bed;
        if (request.bedId() == null) {
            if (request.bedNo() == null || request.bedNo().isBlank()) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), ErrorCodes.VALIDATION_ERROR, "新增铺位时铺位号不能为空");
            }
            long exists = bedMapper.selectCount(new LambdaQueryWrapper<BedEntity>()
                .eq(BedEntity::getRoomId, roomId)
                .eq(BedEntity::getBedNo, request.bedNo()));
            if (exists > 0) {
                throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BUSINESS_CONFLICT, "营房内铺位号已存在");
            }
            bed = new BedEntity();
            bed.setRoomId(roomId);
            bed.setBedNo(request.bedNo());
            bed.setStatus(request.status() == null || request.status().isBlank() ? BedStatus.AVAILABLE : request.status());
            bedMapper.insert(bed);
        } else {
            bed = bedMapper.selectById(request.bedId());
            if (bed == null || !Objects.equals(bed.getRoomId(), roomId)) {
                throw new AppException(HttpStatus.NOT_FOUND.value(), ErrorCodes.RESOURCE_NOT_FOUND, "铺位不存在");
            }
            if (request.bedNo() != null && !request.bedNo().isBlank()) {
                long exists = bedMapper.selectCount(new LambdaQueryWrapper<BedEntity>()
                    .eq(BedEntity::getRoomId, roomId)
                    .eq(BedEntity::getBedNo, request.bedNo())
                    .ne(BedEntity::getId, request.bedId()));
                if (exists > 0) {
                    throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BUSINESS_CONFLICT, "营房内铺位号已存在");
                }
                bed.setBedNo(request.bedNo());
            }
            if (request.status() != null && !request.status().isBlank()) {
                if (BedStatus.DISABLED.equals(request.status()) && BedStatus.OCCUPIED.equals(bed.getStatus())) {
                    throw new AppException(HttpStatus.CONFLICT.value(), ErrorCodes.BUSINESS_CONFLICT, "占用中的铺位不可停用");
                }
                bed.setStatus(request.status());
            }
            bedMapper.updateById(bed);
        }

        CustomerEntity customer = customerMapper.selectOne(new LambdaQueryWrapper<CustomerEntity>()
            .eq(CustomerEntity::getBedId, bed.getId())
            .in(CustomerEntity::getStatus, Set.of(CustomerStatus.RESIDENT, CustomerStatus.OUTING))
            .last("limit 1"));

        return new BedView(
            bed.getId(),
            bed.getRoomId(),
            room.getRoomNo(),
            bed.getBedNo(),
            bed.getStatus(),
            customer == null ? null : customer.getId(),
            customer == null ? null : customer.getName()
        );
    }
}
