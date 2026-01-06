package com.demo.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.demo.api.entity.OrderStatus;

public record OrderDetailResponse (
    Long id,
    String memberUserName,
    OrderStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    List<Line> items
) {
    public record Line (
        Long productId,
        String productName,
        BigDecimal unitPrice,
        BigDecimal qty,
        BigDecimal lineAmount
    ) {}
}
