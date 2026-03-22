package com.skeeterSoftworks.WorkOrderCentral.domain.objects;


import com.skeeterSoftworks.WorkOrderCentral.to.enums.EStationType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class StationInfo {

	@Column
	private String stationID;

	@Column
	private String macAddress;

	@Enumerated(EnumType.STRING)
	@Column
	private EStationType stationType;

	public StationInfo(String stationID) {
		this.stationID = stationID;
	}

}
