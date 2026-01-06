package com.demo.api.dto;

import java.util.List;

public record CreateOrderRequest (
    Long memberId,
    List<Item> items
) {
    public record Item(
        Long productId,
        int qty
    ) {}
}
