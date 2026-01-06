package com.demo.api;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.demo.api.entity.Member;
import com.demo.api.entity.Order;
import com.demo.api.entity.OrderItem;
import com.demo.api.entity.Product;
import com.demo.api.repo.MemberRepository;
import com.demo.api.repo.OrderRepository;
import com.demo.api.repo.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

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
}
