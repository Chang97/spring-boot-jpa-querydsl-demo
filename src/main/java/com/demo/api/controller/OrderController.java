package com.demo.api.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.api.code.OrderStatus;
import com.demo.api.dto.CreateOrderRequest;
import com.demo.api.dto.CreateOrderResponse;
import com.demo.api.dto.DeleteResultResponse;
import com.demo.api.dto.OrderDetailResponse;
import com.demo.api.dto.OrderSearchCond;
import com.demo.api.dto.OrderStatusChangedResponse;
import com.demo.api.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /** 1) 주문 생성 */
    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@RequestBody CreateOrderRequest req) {
        var qtyMap = req.items().stream()
            .collect(Collectors.toMap(CreateOrderRequest.Item::productId, CreateOrderRequest.Item::qty));
        Long id = orderService.create(req.memberId(), qtyMap);
        return ResponseEntity
                .created(URI.create("/api/orders/" + id))
                .body(new CreateOrderResponse(id));
    }

    /* 2) 주문 상세 조회 */
    @GetMapping("{id}")
    public ResponseEntity<OrderDetailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetail(id));
    }

    /* 3) 주문 목록 조회 */
    // @GetMapping
    // public ResponseEntity<List<OrderDetailResponse>> list() {
    //     return ResponseEntity.ok(orderService.getOrderList());
    // }

    /** 4) 상태 변경 */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderStatusChangedResponse> changeStatus(@PathVariable Long id, @RequestParam OrderStatus to) {
        orderService.changeStatus(id, to);
        return ResponseEntity.ok(new OrderStatusChangedResponse(id, to));
    }

    /** 5) 주문 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultResponse> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(new DeleteResultResponse(id, true));
    }

    /** 6) 목록 + 동적 검색 + 페이징 */
    @GetMapping
    public ResponseEntity<Page<OrderDetailResponse>> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) String memberUsernameLike,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort // 예: sort=createdAt,desc&sort=id,desc
    ) {
        Pageable pageable = toPageable(page, size, sort);
        var cond = new OrderSearchCond(status, from, to, minTotal, memberUsernameLike, productId);
        return ResponseEntity.ok(orderService.search(cond, pageable));
    }

    // ---------- helpers ----------

    private Pageable toPageable(int page, int size, List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            // 기본 정렬: 최신순
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        }
        Sort sort = Sort.unsorted();
        for (String s : sortParams) {
            // "field,dir" 또는 "field"
            String[] t = s.split(",", 2);
            String field = t[0].trim();
            Sort.Direction dir = (t.length > 1 ? Sort.Direction.fromString(t[1].trim()) : Sort.Direction.ASC);
            sort = sort.and(Sort.by(dir, field));
        }
        return PageRequest.of(page, size, sort);
    }
    
}
