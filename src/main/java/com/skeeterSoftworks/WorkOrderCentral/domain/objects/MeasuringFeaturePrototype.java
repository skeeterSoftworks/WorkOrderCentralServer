package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasureCheckType;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMeasuringFeatureClassType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = "product")
public class MeasuringFeaturePrototype {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String catalogueId;

	@Column
	private String description;

	@Column(columnDefinition = "boolean default false")
	private boolean absoluteMeasure;

	/** Nominal (ideal) measured value for MEASURED features. */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal refValue;

	/** Minimum acceptable measured value (inclusive). */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal minTolerance;

	/** Maximum acceptable measured value (inclusive). */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal maxTolerance;

	@Enumerated(EnumType.STRING)
	@Column
	private EMeasuringFeatureClassType classType;

	@Column
	private String frequency;


	@Enumerated(EnumType.STRING)
	@Column
	private EMeasureCheckType checkType;

	@Column
	private String toolType;

	@Column
	private String measuringTool;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	@JsonBackReference
	private Product product;

}
