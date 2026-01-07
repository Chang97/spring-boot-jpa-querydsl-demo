package com.demo.api.dto;

import com.demo.api.code.OrderStatus;

public record OrderStatusChangedResponse(Long id, OrderStatus status) {}
