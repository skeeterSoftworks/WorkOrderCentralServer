package com.skeeterSoftworks.WorkOrderCentral.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseDataDTO {

	private String issuedTo;
	private String validFrom;
	private String validUntil;
	private String macAddress;

}
