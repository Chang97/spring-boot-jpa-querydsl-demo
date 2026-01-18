package com.demo.api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.api.code.OrderStatus;
import com.demo.api.dto.OrderDetailResponse;
import com.demo.api.entity.Member;
import com.demo.api.entity.Order;
import com.demo.api.entity.OrderItem;
import com.demo.api.entity.Product;
import com.demo.api.repo.MemberRepository;
import com.demo.api.repo.OrderRepository;
import com.demo.api.repo.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    /** 단건 상세: fetch join 전용 쿼리 사용 (OrderRepository.findDetail) */
    public OrderDetailResponse getOrderDetail(Long id) {
        return toResponse(orderRepository.findDetail(id).orElseThrow());
    }

    /** 전체 목록: 간단 버전 (소규모 데이터/관리 화면용) */
    public List<OrderDetailResponse> getOrderList() {
        return orderRepository.findAllBy().stream().map(this::toResponse).toList();
    }

    /** 상태별 페이지: 두 번 조회 전략 (ID 페이지 → 연관 일괄 로딩) */
    public Page<OrderDetailResponse> getOrderPage(OrderStatus status, Pageable pageable) {
        Page<Long> idPage = orderRepository.findIdsByStatus(status, pageable); // select o.id ...
        if (idPage.isEmpty()) {
        return Page.empty(pageable);
        }
        // 연관 즉시 로딩(멤버만) + 배치 로딩으로 품목/상품 접근 시 N+1 최소화
        List<Order> orders = orderRepository.findWithMemberByIdIn(idPage.getContent());
        List<OrderDetailResponse> content = orders.stream().map(this::toResponse).toList();
        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    /** 생성(집합 루트 기준): cascade + 편의 메서드 이용 */
    @Transactional
    public Long create(Long memberId, Map<Long, Integer> productQtyMap) {
        Member m = memberRepository.findById(memberId).orElseThrow();

        Order order = new Order();
        order.setMember(m);

        BigDecimal total = BigDecimal.ZERO;
        for (var e : productQtyMap.entrySet()) {
        Product p = productRepository.findById(e.getKey()).orElseThrow();
        int qty = e.getValue();
        BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(qty));

        OrderItem item = new OrderItem();
        item.setProduct(p);
        item.setQty(qty);
        item.setUnitPrice(p.getPrice());
        item.setLineAmount(line);

        order.addItem(item);               // 양방향 동기화
        total = total.add(line);
        }
        order.setTotalAmount(total);

        return orderRepository.save(order).getId(); // @GeneratedValue
    }

    /** 상태 변경: 더티 체킹 */
    @Transactional
    public void changeStatus(Long id, OrderStatus to) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus(to);
    }

    /** 삭제: 고아 제거로 품목 자동 삭제 */
    @Transactional
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    // ───────────────────────── private mappers ─────────────────────────

    private OrderDetailResponse toResponse(Order o) {
        return new OrderDetailResponse(
            o.getId(),
            o.getMember().getUsername(),
            o.getStatus(),
            o.getTotalAmount(),
            o.getCreatedAt(),
            o.getItems().stream().map(this::toLine).toList()
        );
    }

    private OrderDetailResponse.Line toLine(OrderItem i) {
        return new OrderDetailResponse.Line(
            i.getProduct().getId(),
            i.getProduct().getName(),
            i.getUnitPrice(),
            i.getQty(),
            i.getLineAmount()
        );
    }
}
