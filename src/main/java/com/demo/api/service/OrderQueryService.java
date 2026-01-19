package com.demo.api.service;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.api.dto.OrderDetailResponse;
import com.demo.api.dto.OrderSearchCond;
import com.demo.api.entity.Order;
import com.demo.api.repo.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public Page<OrderDetailResponse> search(OrderSearchCond cond, Pageable pageable) {
        Pageable page = pageable;
        if (page == null) page = PageRequest.of(0, 20);
        if (page.getSort().isUnsorted()) {
            page = PageRequest.of(page.getPageNumber(), page.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        }

        Page<Order> result = orderRepository.search(cond, page);

        // 목록은 요약만(라인 미포함) — 기존 toResponseSummary와 동일한 개념
        return result.map(this::toSummary);
    }

    public OrderDetailResponse getDetail(Long id) {
        Order o = orderRepository.findDetailFetch(id).orElseThrow();
        return toDetail(o);
    }

    private OrderDetailResponse toSummary(Order o) {
        return new OrderDetailResponse(
            o.getId(),
            o.getMember().getUsername(),
            o.getStatus(),
            o.getTotalAmount(),
            o.getCreatedAt(),
            List.of() // 목록에서는 품목 미포함(상세 전용)
        );
    }

    private OrderDetailResponse toDetail(Order o) {
        return new OrderDetailResponse(
            o.getId(),
            o.getMember().getUsername(),
            o.getStatus(),
            o.getTotalAmount(),
            o.getCreatedAt(),
            o.getItems().stream().map(i ->
                new OrderDetailResponse.Line(
                    i.getProduct().getId(),
                    i.getProduct().getName(),
                    i.getUnitPrice(),
                    i.getQty(),
                    i.getLineAmount()
                )
            ).toList()
        );
    }
}
