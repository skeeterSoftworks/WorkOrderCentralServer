package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EProductStockIntakeUnitOfMeasure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"product", "workOrder"})
public class ProductStockIntake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Column(length = 128)
    private String stickerNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EProductStockIntakeUnitOfMeasure unitOfMeasure = EProductStockIntakeUnitOfMeasure.PIECES;

    @Column(nullable = false)
    private int quantity;

    /** Portion of {@link #quantity} classified as surplus stock (internal PO or above order quantity). */
    @Column(nullable = false)
    private int surplusQuantity = 0;

    @Column(nullable = false)
    private LocalDateTime receivedAt;
}
