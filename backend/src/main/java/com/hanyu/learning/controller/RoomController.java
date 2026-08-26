package com.hanyu.learning.controller;

import com.hanyu.learning.common.api.ApiResponse;
import com.hanyu.learning.dto.request.SaveBedRequest;
import com.hanyu.learning.dto.request.SaveRoomRequest;
import com.hanyu.learning.dto.view.BedView;
import com.hanyu.learning.dto.view.RoomView;
import com.hanyu.learning.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ApiResponse<List<RoomView>> listRooms(@RequestParam(defaultValue = "false") boolean includeBeds) {
        return ApiResponse.success(roomService.listRooms(includeBeds));
    }

    @PostMapping
    public ApiResponse<RoomView> saveRoom(@Valid @RequestBody SaveRoomRequest request) {
        return ApiResponse.success(roomService.saveRoom(request));
    }

    @PostMapping("/{roomId}/beds")
    public ApiResponse<BedView> saveBed(@PathVariable Long roomId, @Valid @RequestBody SaveBedRequest request) {
        return ApiResponse.success(roomService.saveBed(roomId, request));
    }
}
