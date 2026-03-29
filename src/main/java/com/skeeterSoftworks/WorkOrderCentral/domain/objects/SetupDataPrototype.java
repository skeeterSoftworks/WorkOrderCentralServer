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

	/** Nominal (ideal) diameter for setup measurement. */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal diameterRefValue;

	/** Maximum allowed measured diameter (inclusive). */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal diameterMaxPosTolerance;

	/** Minimum allowed measured diameter (inclusive). */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal diameterMaxNegTolerance;

	/** Nominal (ideal) height for setup measurement. */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal heightRefValue;

	/** Maximum allowed measured height (inclusive). */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal heightMaxPosTolerance;

	/** Minimum allowed measured height (inclusive). */
	@Column(columnDefinition="Decimal(10,5)")
	private BigDecimal heightMaxNegTolerance;

	@Column
	private boolean isAttributiveHeightMeasurement = false;

	@Column
	private boolean isAttributiveDiameterMeasurement = false;


}
