package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Immutable copy of a technology tool taken when a work session opens.
 */
@Entity
@Table(name = "work_session_tool_usage")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "technologySnapshot" })
@EqualsAndHashCode(exclude = { "technologySnapshot" })
public class WorkSessionToolUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "technology_snapshot_id", nullable = false)
    private WorkSessionTechnologySnapshot technologySnapshot;

    /** Catalogue tool id at snapshot time (audit only). */
    @Column
    private Long sourceToolId;

    @Column
    private String toolName;

    @Column(length = 2000)
    private String toolDescription;

    @Column
    private Integer orderNumber;

    @Column
    private Integer workingTime;
}
