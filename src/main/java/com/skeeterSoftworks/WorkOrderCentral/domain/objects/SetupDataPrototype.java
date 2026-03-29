package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class SetupDataPrototype {

	@Column
	private String operationID;

	@Column
	private String toolID;

	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal diameterRefValue;

	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal diameterMaxPosTolerance;

	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal diameterMaxNegTolerance;

	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal heightRefValue;

	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal heightMaxPosTolerance;

	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal heightMaxNegTolerance;

	@Column
	private boolean isAttributiveHeightMeasurement = false;

	@Column
	private boolean isAttributiveDiameterMeasurement = false;


}
