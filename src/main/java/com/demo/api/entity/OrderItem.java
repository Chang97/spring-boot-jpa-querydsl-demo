package com.demo.api.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity 
@Table(name = "order_item")
@SequenceGenerator(name = "order_item_seq_gen", sequenceName = "order_item_seq", allocationSize = 50)
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 18, scale = 2) 
    private BigDecimal unitPrice;

    @Column(nullable = false) 
    private Integer qty;

    @Column(nullable = false, precision = 18, scale = 2) 
    private BigDecimal lineAmount;

    @Column(nullable = false) 
    private Instant createdAt = Instant.now();

    public void setOrder(Order order) { 
        this.order = order; 
    }

    public void setProduct(Product product) { 
        this.product = product; 
    }
}