package com.demo.api.spec;

import org.springframework.data.jpa.domain.Specification;

import com.demo.api.code.OrderStatus;
import com.demo.api.entity.Order;

import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.math.BigDecimal;

public final class OrderSpecs {
    private OrderSpecs() {}

    public static Specification<Order> statusEq(OrderStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Order> createdBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        if (from != null && to != null) return (r, q, cb) -> cb.between(r.get("createdAt"), from, to);
        if (from != null) return (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("createdAt"), from);
        return (r, q, cb) -> cb.lessThanOrEqualTo(r.get("createdAt"), to);
    }

    public static Specification<Order> totalGte(BigDecimal minTotal) {
        if (minTotal == null) return null;
        return (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("totalAmount"), minTotal);
    }

    public static Specification<Order> memberUsernameLike(String like) {
        if (like == null || like.isBlank()) return null;
        String pattern = "%" + like.trim() + "%";
        return (root, q, cb) -> {
            // 다대일 LAZY 조인: 페이징 시 count 쿼리 영향 최소화를 위해 필요시에만 join
            var m = root.join("member", JoinType.LEFT);
            return cb.like(m.get("username"), pattern);
        };
    }

    // 예: 품목에 특정 상품이 포함된 주문
    public static Specification<Order> hasProductId(Long productId) {
        if (productId == null) return null;
        return (root, q, cb) -> {
            var items = root.join("items");          // 컬렉션 조인: 중복 로우 주의
            var p = items.join("product");
            if (q != null) q.distinct(true);             // 결과 중복 제거
            return cb.equal(p.get("id"), productId);
        };
    }
}
