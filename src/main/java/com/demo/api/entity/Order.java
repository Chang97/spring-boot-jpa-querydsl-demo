package com.demo.api.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.demo.api.code.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "orders")
@SequenceGenerator(name = "order_seq_gen", sequenceName = "order_seq")
@Getter @Setter
public class Order extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq_gen")
    private Long id;

    // 다대일은 지연 로딩(LAZY) 권장: 실제 사용할 때만 DB 조회
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "member_id",                // FK 컬럼명 지정
        nullable = false,                  // DB NOT NULL 제약
        foreignKey = @ForeignKey(name="fk_orders_member") // FK 제약 이름(가독성↑)
    )
    private Member member;

    @Enumerated(EnumType.STRING) // 이름으로 저장(순서 저장 금지)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,     // 부모→자식 생명주기 전이
        orphanRemoval = true           // 고아 제거(컬렉션에서 제거 시 DELETE)
    )
    private List<OrderItem> items = new ArrayList<>();

    @Version // 동시 수정 충돌 감지(낙관적 락)
    private Long version;

    // 양쪽을 함께 세팅하는 편의 메서드(연관관계 일관성)
    public void addItem(OrderItem i) {
        items.add(i);
        i.setOrder(this);
    }

    public void removeItem(OrderItem i) {
        items.remove(i);
        i.setOrder(null); // orphanRemoval=true 이면 DB에서 자동 삭제
    }
}
