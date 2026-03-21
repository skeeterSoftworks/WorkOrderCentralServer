package com.skeeterSoftworks.WorkOrderCentral.facade;


import com.skeeterSoftworks.WorkOrderCentral.service.UsersService;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.UserTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UsersFacade {

	UsersService usersService;


	@Autowired
	public UsersFacade(UsersService usersService) {
		this.usersService = usersService;
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllUsers() {

		try {
			return ResponseEntity.ok(usersService.getAllUsers());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR_FETCHING_USERS");
		}

	}

	@PostMapping("/add")
	public ResponseEntity<Void> addUser(@RequestBody UserTO userTO) {

		log.debug("Facade call: addUser()");

		if (!StringUtils.hasText(userTO.getName()) || !StringUtils.hasText(userTO.getSurname()) ||
				userTO.getRole() == null || !StringUtils.hasText(userTO.getQrCode())) {
			log.error("Invalid input params!: {}", userTO);
			return ResponseEntity.badRequest().build();
		}

		try {
			usersService.addUser(userTO);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/update")
	public ResponseEntity<Void> updateUser(@RequestBody UserTO userTO) {

		log.debug("Facade call: updateUser()");

		if (!StringUtils.hasText(userTO.getName()) || !StringUtils.hasText(userTO.getSurname()) || userTO.getRole() == null ||
				userTO.getId() < 1 || !StringUtils.hasText(userTO.getQrCode())) {
			log.error("Invalid input params!: {}", userTO);
			return ResponseEntity.badRequest().build();
		}

		try {
			usersService.updateUser(userTO);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/{qrCode}")
	public ResponseEntity<?> getSingleUser(@PathVariable String qrCode) {
		log.debug("Facade call: getSingleUser({})", qrCode);

		try {
			return ResponseEntity.ok(usersService.getUserByQrCode(qrCode));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

		log.debug("Facade call: deleteUser({})", id);

		if (id == null || id < 1) {
			log.error("Invalid user id: {}", id);
			return ResponseEntity.badRequest().build();
		}

		try {
			usersService.deleteUser(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
