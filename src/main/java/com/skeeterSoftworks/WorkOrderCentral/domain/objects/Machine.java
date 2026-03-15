package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "tools")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String machineName;

    @Column
    private Long cycleTime;

    @Column
    private String barLocation;

    @Column
    private Long piecesPerBar;

    @Column
    private Long barsPerSeries;

    @Column
    private Long barsCount;

    @Column
    private Double weightPerBar;

    @Column
    private Double sumBarWeight;

    @Column
    private String seriesID;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Tool> tools = new ArrayList<>();

}
