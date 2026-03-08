package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ApplicationUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<ApplicationUser, Long>{

	ApplicationUser findByQrCode(String qrCode);

	@Query("SELECT qrCode from ApplicationUser applicationUser")
	List<String> findAllQrs();

	List<ApplicationUser> findAll();
}
