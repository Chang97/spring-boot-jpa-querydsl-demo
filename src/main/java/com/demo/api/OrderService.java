package com.demo.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    public OrderDetailResponse getOrderDetail(Long id) {
        return toResponse(orderRepository.findDetail(id).orElseThrow());
    }

    public List<OrderDetailResponse> getOrderList() {
        return orderRepository.findAllBy().stream().map(this::toResponse).toList();
    }
    
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

            order.addItem(item);
            total = total.add(line);
        }
        order.setTotalAmount(total);

        return orderRepository.save(order).getId(); // @GeneratedValue로 PK 채번
    }

    @Transactional
    public Order changeStatus(Long id, OrderStatus to) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus(to);
        return orderRepository.save(o);
    }

    @Transactional
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

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
