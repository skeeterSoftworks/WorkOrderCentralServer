package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
public class WorkSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Embedded
	private Operator operator;

	@Column
	private LocalDateTime sessionStart;

	@Column
	private LocalDateTime sessionEnd;

	@Embedded
	private StationInfo stationInfo;

	@Column
	private long productCount;

	@Column
	private long rusCount;

	@Column
	private String productReferenceID;



	public WorkSession(long id, Operator operator, LocalDateTime sessionStart, LocalDateTime sessionEnd) {
		super();
		this.id = id;
		this.operator = operator;
		this.sessionStart = sessionStart;
		this.sessionEnd = sessionEnd;
	}

	public WorkSession(long id, LocalDateTime sessionStart, LocalDateTime sessionEnd, String operatorQrCode,
                       String stationId, long productCount) {
		super();
		this.id = id;
		this.sessionStart = sessionStart;
		this.sessionEnd = sessionEnd;
		this.productCount = productCount;
		if (this.operator == null){
			this.operator = new Operator();
		}
		this.operator.setOperatorQrCode(operatorQrCode);

		if (this.stationInfo == null){
			this.stationInfo = new StationInfo();
		}
		this.stationInfo.setStationID(stationId);
	}
}
