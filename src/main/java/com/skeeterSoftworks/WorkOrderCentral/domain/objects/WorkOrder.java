package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "purchaseOrder", "machineBookings" })
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id", unique = true)
    private PurchaseOrder purchaseOrder;

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

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MachineBooking> machineBookings = new ArrayList<>();

}
