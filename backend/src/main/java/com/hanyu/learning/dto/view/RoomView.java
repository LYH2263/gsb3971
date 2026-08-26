package com.hanyu.learning.dto.view;

import java.util.List;

public record RoomView(Long id, Integer floor, String roomNo, Integer status, List<BedView> beds) {
}
