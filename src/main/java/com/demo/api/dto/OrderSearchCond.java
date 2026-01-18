package com.demo.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.demo.api.code.OrderStatus;

public record OrderSearchCond(
    OrderStatus status,
    Instant from,
    Instant to,
    BigDecimal minTotal,
    String memberUsernameLike,
    Long productId
) {}
