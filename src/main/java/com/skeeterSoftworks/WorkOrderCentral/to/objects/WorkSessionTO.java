package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkSessionTO {

    private Long id;
    private Long workOrderId;
    private LocalDateTime sessionStart;
    private LocalDateTime sessionEnd;
    private long productCount;
    /** Number of control products recorded in this session. */
    private long controlProductCount;
    /** Number of faulty products recorded in this session. */
    private long faultyProductCount;
    /** Number of setup (e.g. tool change) events recorded in this session. */
    private long setupProductCount;
    /** Recorded setup events with prototype snapshot and measured values. */
    private List<SetupProductTO> setupProducts;
    private String productReferenceID;
    private String operatorQrCode;
    private String operatorName;
    private String operatorSurname;
    private String stationId;
    /** True when this response follows reaching the work order production target (session auto-closed). */
    private boolean workOrderCompletedByTarget;

    /** Measuring feature prototypes for this work order's product (for production control). */
    private List<MeasuringFeaturePrototypeTO> measuringFeaturePrototypes;

    /** Raw Base64 of the product technical drawing (when present). */
    private String technicalDrawingBase64;
}
