package com.skeeterSoftworks.WorkOrderCentral.mapper;


import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ApplicationUser;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.ERole;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.UserTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UsersMapperService {

	public void mapUserList2UserTOList(List<ApplicationUser> usersList, List<UserTO> userTOList) {

		for (ApplicationUser user : usersList) {
			userTOList.add(mapUser2UserTO(user));
		}
	}

	public UserTO mapUser2UserTO(ApplicationUser user) {

		UserTO userTO = new UserTO();
		userTO.setName(user.getName());
		userTO.setSurname(user.getSurname());
		userTO.setRoles(user.getRoles() != null ? new ArrayList<>(user.getRoles()) : new ArrayList<>());
		userTO.setQrCode(user.getQrCode());
		userTO.setCreatedDate(user.getCreatedDate());
		userTO.setId(user.getId());

		return userTO;
	}

	public void mapUserTOList2UserList(List<UserTO> userTOList, List<ApplicationUser> userList) {

		for (UserTO userTO : userTOList) {

			ApplicationUser user = new ApplicationUser();

			mapUserTO2User(userTO, user);
			userList.add(user);
		}

	}

	public void mapUserTO2User(UserTO userTO, ApplicationUser user) {

		user.setName(userTO.getName());
		user.setSurname(userTO.getSurname());
		user.setQrCode(userTO.getQrCode());

		if (userTO.getRoles() != null && !userTO.getRoles().isEmpty()) {
			user.setRoles(new HashSet<>(userTO.getRoles()));
		}

		if (userTO.getCreatedDate() != null) {
			user.setCreatedDate(userTO.getCreatedDate());
		}

	}

	public static boolean hasAnyRole(ApplicationUser user, ERole... roles) {
		if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
			return false;
		}
		Set<ERole> userRoles = user.getRoles();
		if (userRoles.contains(ERole.ADMIN)) {
			return true;
		}
		for (ERole role : roles) {
			if (userRoles.contains(role)) {
				return true;
			}
		}
		return false;
	}
}
