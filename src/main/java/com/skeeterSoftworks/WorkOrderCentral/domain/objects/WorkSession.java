package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
public class WorkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Session belongs to one work order. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Embedded
    private Operator operator;

    @Column
    private LocalDateTime sessionStart;

    @Column
    private LocalDateTime sessionEnd;

    @Embedded
    private StationInfo stationInfo;

    @Column
    private long productCount;

    @Column
    private String productReferenceID;

    /** Count of setup / tool-change events recorded for this session (kept in sync with {@link #setupProducts}). */
    @Column
    private Long setupProductCount;

    @OneToMany(mappedBy = "workSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SetupProduct> setupProducts = new ArrayList<>();

    @OneToMany(mappedBy = "workSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FaultyProduct> faultyProducts = new ArrayList<>();

    @OneToMany(mappedBy = "workSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ControlProduct> controlProducts = new ArrayList<>();

    public WorkSession(long id, Operator operator, LocalDateTime sessionStart, LocalDateTime sessionEnd) {
        super();
        this.id = id;
        this.operator = operator;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
    }

    public WorkSession(long id, LocalDateTime sessionStart, LocalDateTime sessionEnd, String operatorQrCode,
                       String stationId, long productCount) {
        super();
        this.id = id;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
        this.productCount = productCount;
        if (this.operator == null) {
            this.operator = new Operator();
        }
        this.operator.setOperatorQrCode(operatorQrCode);

        if (this.stationInfo == null) {
            this.stationInfo = new StationInfo();
        }
        this.stationInfo.setStationID(stationId);
    }
}
