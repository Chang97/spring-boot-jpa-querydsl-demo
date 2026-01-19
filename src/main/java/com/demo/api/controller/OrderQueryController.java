package com.demo.api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.demo.api.dto.OrderDetailResponse;
import com.demo.api.dto.OrderSearchCond;
import com.demo.api.service.OrderQueryService;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/q-orders")
public class OrderQueryController {

    private final OrderQueryService service;

    @GetMapping
    public ResponseEntity<Page<OrderDetailResponse>> search(OrderSearchCond cond, Pageable pageable) {
        return ResponseEntity.ok(service.search(cond, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }
}