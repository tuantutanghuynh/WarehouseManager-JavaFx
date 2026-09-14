package com.warehousemanager.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "goods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoodsEntity {

    @Id
    @Column(name = "goods_code", length = 20)
    private String goodsCode;

    @Column(name = "goods_name", nullable = false, length = 100)
    private String goodsName;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "mins_stock_level", nullable = false)
    private Integer minsStockLevel = 5;

    @Column(name = "goods_type", nullable = false, length = 1)
    private String goodsType; // 'R' = RawMaterial, 'F' = FinishedProduct

    @Column(length = 100)
    private String supplier;

    @Column(name = "sell_price")
    private Double sellPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Tính giá trị tồn kho
    public Double getStockValue() {
        if ("R".equalsIgnoreCase(goodsType)) {
            return quantity * 1.2;
        } else if ("F".equalsIgnoreCase(goodsType)) {
            return quantity * (sellPrice != null ? sellPrice : 0.0);
        }
        return 0.0;
    }
}
