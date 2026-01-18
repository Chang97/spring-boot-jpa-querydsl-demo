package com.demo.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "catalog_items",
    indexes = {
        @Index(name = "ix_catalog_items_sku", columnList = "sku")
    },
    uniqueConstraints = {
        // 복합 유니크: (brand, name) 조합은 한 번만 허용
        @UniqueConstraint(name = "uk_brand_name", columnNames = {"brand", "name"})
    }
    )
@SequenceGenerator(name = "catalog_item_seq_gen", sequenceName = "catalog_item_seq")
@Getter @Setter
public class CatalogItem extends BaseAuditableEntity {

    /** ───────── 식별자 ───────── */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalog_item_seq_gen")
    private Long id;

    /** ───────── 컬럼 기본 ───────── */
    @Column(nullable = false, length = 80)           // NULL 금지 + 길이
    private String brand;

    @Column(nullable = false, length = 120)          // NULL 금지 + 길이
    private String name;

    // 단일 컬럼 유니크(복합 유니크는 @Table에 선언)
    @Column(nullable = false, length = 40, unique = true)
    private String sku;

    /** ───── 이름/DDL 제어 ───── */
    // DB 물리 컬럼명을 따로 주고 싶을 때
    @Column(name = "display_name", length = 150)
    private String displayName;

    // DB별 DDL을 직접 쓰는 경우(남용 금지)
    // 타입만 columnDefinition에 유지
    @Column(name = "market", columnDefinition = "citext", nullable = false)
    @org.hibernate.annotations.ColumnDefault("'KR'")
    @org.hibernate.annotations.Comment("market code")
    private String market = "KR";

    /** ───── 숫자/금액 ───── */
    @Column(nullable = false, precision = 18, scale = 2) // 999,999,999,999,999.99
    private BigDecimal listPrice;

    /** ───── 대용량/변환 ───── */
    @Lob                                             // TEXT/CLOB 매핑(설명 등 장문)
    @Basic(fetch = FetchType.LAZY)
    private String description;

    // 'Y'/'N' 문자열로 저장하고 boolean 으로 사용
    @Convert(converter = YesNoBooleanConverter.class)
    @Column(length = 1, nullable = false)
    private boolean discontinued;

    /** ───── Enum/낙관적 락 ───── */
    public enum ItemStatus { DRAFT, ACTIVE, ARCHIVED }

    @Enumerated(EnumType.STRING)                      // 문자열로 안전하게 저장
    @Column(nullable = false, length = 16)
    private ItemStatus status = ItemStatus.DRAFT;

    @Version                                         // 동시 수정 충돌 감지(낙관적 락)
    private Long version;
}