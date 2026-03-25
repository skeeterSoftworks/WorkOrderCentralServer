package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EWorkOrderState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Do not delete WorkOrder; WO is the execution of the ProductOrder.
 *
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "productOrder", "machineBookings", "workSessions" })
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One work order per purchase-order line (product line item). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_order_id", referencedColumnName = "id", unique = true)
    private ProductOrder productOrder;

    @Column
    private LocalDate dueDate;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @JsonManagedReference
    private List<Material> materials = new ArrayList<>();

    @Column
    private String comment;

    /**
     * Aggregated count of good products produced across all work sessions for this work order.
     */
    @Column
    private long producedGoodQuantity;

    @Enumerated(EnumType.STRING)
    @Column
    private EWorkOrderState state = EWorkOrderState.INCOMPLETE;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MachineBooking> machineBookings = new ArrayList<>();

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkSession> workSessions = new ArrayList<>();
}
