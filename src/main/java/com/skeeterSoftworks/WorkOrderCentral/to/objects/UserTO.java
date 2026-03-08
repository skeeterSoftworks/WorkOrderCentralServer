package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.ERole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class UserTO {

	private String name;
	private String surname;
	private ERole role;
	private String qrCode;
	private long id;

	private LocalDateTime createdDate;
}
