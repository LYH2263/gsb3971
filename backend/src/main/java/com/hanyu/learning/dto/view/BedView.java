package com.hanyu.learning.dto.view;

public record BedView(Long id, Long roomId, String roomNo, String bedNo, String status, Long customerId, String customerName) {
}
