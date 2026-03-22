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
public class Operator {

	@Column
	private String operatorQrCode;

	@Column
	private String name;

	@Column
	private String surname;

	public Operator(String name, String surname) {
		this.name = name;
		this.surname = surname;
	}

}
