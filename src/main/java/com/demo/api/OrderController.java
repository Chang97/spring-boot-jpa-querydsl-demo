package com.demo.api;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
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
import com.demo.api.dto.OrderStatusChangedResponse;

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
    @GetMapping
    public ResponseEntity<List<OrderDetailResponse>> list() {
        return ResponseEntity.ok(orderService.getOrderList());
    }

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
}
