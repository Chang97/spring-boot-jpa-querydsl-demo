package com.demo.api.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.demo.api.dto.OrderSearchCond;
import com.demo.api.entity.Order;

public interface OrderQueryRepository {
    Page<Order> search(OrderSearchCond cond, Pageable pageable);    // 목록은 엔티티 반환(서비스에서 DTO 매핑)
    Optional<Order> findDetailFetch(Long id);                       // 상세는 fetch join
}
