package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class SetupDataPrototype {


	@Column
	private String operationID;

	@Column
	private String toolID;

	@Column
	private String measuredDiameter;

	@Column
	private String measuredHeight;

	@Column
	private String setupReason;


}
