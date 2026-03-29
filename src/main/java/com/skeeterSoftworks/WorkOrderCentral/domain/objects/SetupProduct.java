package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_session_id")
    private WorkSession workSession;

    @Column
    private LocalDateTime recordedAt;

    /** Snapshot of {@link Product#getSetupDataPrototype()} at record time (may be null). */
    @Embedded
    private SetupDataPrototype prototypeSnapshot;

    @Column
    private String measuredHeight;

    @Column
    private Boolean measuredHeightOk;

    @Column
    private String measuredDiameter;

    @Column
    private Boolean measuredDiameterOk;
}
