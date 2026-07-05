package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.ERole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class UserTO {

	private String name;
	private String surname;
	private List<ERole> roles = new ArrayList<>();
	private String qrCode;
	private Long id;

	private LocalDateTime createdDate;
}
