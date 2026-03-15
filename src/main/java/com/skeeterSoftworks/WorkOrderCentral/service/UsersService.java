package com.skeeterSoftworks.WorkOrderCentral.service;


import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ApplicationUser;
import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.UserRepository;
import com.skeeterSoftworks.WorkOrderCentral.mapper.UsersMapperService;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.ERole;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.UserTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class UsersService {

	UserRepository usersRepository;

	UsersMapperService usersMapperService;

	@Value("${mock.adminEnabled:false}")
	private boolean mockAdminEnabled;

	@PostConstruct
	private void init() {

		if (mockAdminEnabled) {

			ApplicationUser user = usersRepository.findByQrCode("0000001");

			if (user == null) {
				user = new ApplicationUser();
				user.setQrCode("0000001");
				user.setName("User");
				user.setSurname("Test Admin");
				user.setRole(ERole.ADMIN);
				user.setCreatedDate(LocalDateTime.now());

				usersRepository.save(user);
			}

		}
	}

	@Autowired
	public UsersService(UserRepository usersRepository, UsersMapperService usersMapperService) {
		this.usersRepository = usersRepository;
		this.usersMapperService = usersMapperService;

	}

	public List<UserTO> getAllUsers() {

        return usersRepository.findAll().stream().map(user -> usersMapperService.mapUser2UserTO(user)).toList();
	}

	public void addUser(UserTO userTO) {

		ApplicationUser user = new ApplicationUser();

		usersMapperService.mapUserTO2User(userTO, user);

		user.setCreatedDate(LocalDateTime.now());

		List<String> qrCodesInUse = usersRepository.findAllQrs();

		if (StringUtils.hasText(user.getQrCode())) {

			Random random = new Random();

			user.setQrCode(String.valueOf(random.nextInt(100000000, 999999999)));

		} else if (qrCodesInUse != null && !qrCodesInUse.isEmpty() && qrCodesInUse.contains(user.getQrCode())) {
			throw new IllegalArgumentException("QR_CODE_ALREADY_IN_USE");
		}

		usersRepository.save(user);

		log.info("Successfully saved user {}", user);
	}

	public void updateUser(UserTO userTO) throws Exception {

		Optional<ApplicationUser> userOpt = usersRepository.findById(userTO.getId());

		if (userOpt.isPresent()) {

			ApplicationUser user = userOpt.get();

			usersMapperService.mapUserTO2User(userTO, user);

			usersRepository.save(user);

			log.info("Successfully updated user {}", user);

		} else {

			log.error("User with the given ID not found {}", userTO.getId());
			throw new Exception("USER_NOT_FOUND");
		}
	}

	public UserTO getUserByQrCode(String qrCode) throws Exception {

		ApplicationUser user = usersRepository.findByQrCode(qrCode);

		if (user != null) {
            return usersMapperService.mapUser2UserTO(user);

		} else {
			throw new Exception("USER_NOT_FOUND");
		}
	}

	public void deleteUser(Long id) throws Exception {

		Optional<ApplicationUser> userOpt = usersRepository.findById(id);

		if (userOpt.isPresent()) {
			usersRepository.deleteById(id);
			log.info("Successfully deleted user with id {}", id);
		} else {
			log.error("User with the given ID not found {}", id);
			throw new Exception("USER_NOT_FOUND");
		}
	}

}
