package com.demo.api.repo;

import static com.querydsl.core.types.ExpressionUtils.allOf;
import static com.querydsl.core.types.ExpressionUtils.anyOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.demo.api.code.OrderStatus;
import com.demo.api.dto.OrderSearchCond;
import com.demo.api.entity.Order;
import com.demo.api.entity.QMember;
import com.demo.api.entity.QOrder;
import com.demo.api.entity.QOrderItem;
import com.demo.api.entity.QProduct;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

    @Repository
    @RequiredArgsConstructor
    public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory query;

    private static final QOrder o = QOrder.order;
    private static final QMember m = QMember.member;
    private static final QOrderItem i = QOrderItem.orderItem;
    private static final QProduct p = QProduct.product;

    @Override
    public Page<Order> search(OrderSearchCond cond, Pageable pageable) {
        // 동적 where 구성
        Predicate andPart = allOf(
            statusEq(cond.status()),
            createdBetween(cond.from(), cond.to()),
            totalGte(cond.minTotal())
        );

        Predicate orBlock = anyOf(
            memberUsernameLike(cond.memberUsernameLike()),
            hasProductId(cond.productId())    // 이 조건이 있을 때만 items/product 조인 필요
        );

        // 기본 from/join — 목록: to-one만 조인(fetchJoin 아님)
        JPAQuery<Order> contentQuery = query
            .selectFrom(o)
            .join(o.member, m); // 목록은 연관 접근 최소화, fetchJoin 사용하지 않음

        // productId가 있을 때만 품목/상품 조인(중복/불필요 조인 방지)
        if (cond.productId() != null) {
            contentQuery.leftJoin(o.items, i).leftJoin(i.product, p);
        }

        // null-safe where
        Predicate where = allOf(andPart, orBlock);
        if (where != null) contentQuery.where(where);

        contentQuery
            .orderBy(orderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        List<Order> content = contentQuery.fetch();

        // count 쿼리 — content와 동일한 조인 조건 유지
        JPAQuery<Long> countQuery = query
            .select(cond.productId() != null ? o.id.countDistinct() : o.count())
            .from(o)
            .join(o.member, m);

        if (cond.productId() != null) {
            countQuery.leftJoin(o.items, i).leftJoin(i.product, p);
        }
        if (where != null) countQuery.where(where);

        long total = countQuery.fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<Order> findDetailFetch(Long id) {
        Order result = query
            .selectFrom(o)
            .join(o.member, m).fetchJoin()
            .leftJoin(o.items, i).fetchJoin()
            .leftJoin(i.product, p).fetchJoin()
            .where(o.id.eq(id))
            .distinct() // 컬렉션 fetch join 중복 제거
            .fetchOne();
        return Optional.ofNullable(result);
    }

    // ===== where 헬퍼 =====

    private BooleanExpression statusEq(OrderStatus s) {
        return s == null ? null : o.status.eq(s);
    }

    private BooleanExpression createdBetween(java.time.Instant from, java.time.Instant to) {
        if (from == null && to == null) return null;
        if (from != null && to != null) return o.createdAt.between(from, to);
        return from != null ? o.createdAt.goe(from) : o.createdAt.loe(to);
    }

    private BooleanExpression totalGte(java.math.BigDecimal min) {
        return min == null ? null : o.totalAmount.goe(min);
    }

    private BooleanExpression memberUsernameLike(String like) {
        return (like == null || like.isBlank()) ? null : m.username.containsIgnoreCase(like);
    }

    private BooleanExpression hasProductId(Long productId) {
        return productId == null ? null : p.id.eq(productId); // 조인은 상단에서 조건부로 수행
    }

    private OrderSpecifier<?>[] orderSpecifiers(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier<?>[]{ o.createdAt.desc(), o.id.desc() };
        }
        List<OrderSpecifier<?>> list = new ArrayList<>();
        sort.forEach(order -> {
            boolean asc = order.isAscending();
            // 허용 목록(allow-list) 기반 매핑: 지정된 키만 허용
            switch (order.getProperty()) {
                case "createdAt"   -> list.add(asc ? o.createdAt.asc()    : o.createdAt.desc());
                case "id"          -> list.add(asc ? o.id.asc()           : o.id.desc());
                case "totalAmount" -> list.add(asc ? o.totalAmount.asc()  : o.totalAmount.desc());
                case "status"      -> list.add(asc ? o.status.asc()       : o.status.desc());
                default -> { /* 허용 목록 외 키는 무시 */ }
            }
        });
        if (list.isEmpty()) list.add(o.createdAt.desc());
        return list.toArray(OrderSpecifier[]::new);
    }
}