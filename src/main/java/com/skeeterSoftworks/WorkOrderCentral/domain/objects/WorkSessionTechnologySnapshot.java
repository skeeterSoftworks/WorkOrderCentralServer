package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable copy of product technology taken when a work session opens.
 */
@Entity
@Table(name = "work_session_technology_snapshot")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "workSession", "tools" })
@EqualsAndHashCode(exclude = { "workSession", "tools" })
public class WorkSessionTechnologySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "work_session_id", nullable = false, unique = true)
    private WorkSession workSession;

    /** Catalogue technology id at snapshot time (audit only). */
    @Column
    private Long sourceTechnologyId;

    @Column
    private String cycleTime;

    @Column
    private Integer norm100;

    @Column
    private Integer piecesPerMaterial;

    @OneToMany(mappedBy = "technologySnapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkSessionToolUsage> tools = new ArrayList<>();
}
