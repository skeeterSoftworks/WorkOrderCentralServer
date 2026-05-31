package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MaterialOrderReceptionInternalControl {

    @ElementCollection
    @CollectionTable(
            name = "mor_ic_diameter_samples",
            joinColumns = @JoinColumn(name = "material_order_reception_id"))
    @Column(name = "sample_value")
    @OrderColumn(name = "sample_index")
    private List<Float> diameterSamples = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "mor_ic_length_samples",
            joinColumns = @JoinColumn(name = "material_order_reception_id"))
    @Column(name = "sample_value")
    @OrderColumn(name = "sample_index")
    private List<Float> lengthSamples = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "mor_ic_width_samples",
            joinColumns = @JoinColumn(name = "material_order_reception_id"))
    @Column(name = "sample_value")
    @OrderColumn(name = "sample_index")
    private List<Float> widthSamples = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "mor_ic_weight_samples",
            joinColumns = @JoinColumn(name = "material_order_reception_id"))
    @Column(name = "sample_value")
    @OrderColumn(name = "sample_index")
    private List<Float> weightSamples = new ArrayList<>();

    @Column
    private Float overallWeight;

    @Column
    private Boolean overallAcceptance;
}
