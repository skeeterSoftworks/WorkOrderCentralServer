package com.skeeterSoftworks.WorkOrderCentral.tasks;


import com.skeeterSoftworks.WorkOrderCentral.domain.repositories.PurchaseOrderRepository;
import com.skeeterSoftworks.WorkOrderCentral.to.LicenseDataDTO;
import com.skeeterSoftworks.WorkOrderCentral.util.LicenseUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class LicenseChecker {


	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private PurchaseOrderRepository purchaseOrderRepository;

	@Value("${license.key:none}")
	private String licenseKey;

	@PostConstruct
	private void init() {
		try {

			if ("none".equals(licenseKey)) {
				log.warn("-------------------------------------------------------------");
				log.warn("No license found, please apply for one from the developer.");
				log.warn("Trial period will end 6 months after first purchase order save to Database.");
				log.warn("-------------------------------------------------------------");

			} else {

				List<String> macAddresses = LicenseUtils.getMacAddresses();

				ObjectMapper mapper = new ObjectMapper();

				LicenseDataDTO licenseData = mapper.readValue(licenseKey, LicenseDataDTO.class);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				LocalDate validUntil = null;

				if (StringUtils.hasText(licenseData.getValidUntil())) {
					validUntil = LocalDate.parse(licenseData.getValidUntil(), formatter);
				}

				if (!macAddresses.contains(licenseData.getMacAddress())) {
					log.warn("License invalid!");
					SpringApplication.exit(appContext, () -> 0);
				}

				if (validUntil != null) {
					log.info("-------------------------------------------------------------");
					log.info("License found, valid until: {}", validUntil);
					log.info("-------------------------------------------------------------");

					if (validUntil.isBefore(LocalDate.now())) {
						log.warn("License expired, please apply for a new one from the developer!");
						SpringApplication.exit(appContext, () -> 0);
					}

				} else {
					log.info("-------------------------------------------------------------");
					log.info("License active.");
					log.info("-------------------------------------------------------------");

				}
			}

		} catch (Exception e) {
			log.error("Unable to check license!");
			log.error(e.getMessage(), e);
			SpringApplication.exit(appContext, () -> 0);
		}
	}

	@Scheduled(cron = "0/10 * * * * *")
	public void checkLicense() {

		try {

			if ("none".equals(licenseKey)) {
				 log.warn("No license found, please apply for one from the developer.");
				 log.warn("Trial period will end 6 months after first purchase order save to Database.");

			} else {

				List<String> macAddresses = LicenseUtils.getMacAddresses();

				ObjectMapper mapper = new ObjectMapper();

				LicenseDataDTO licenseData = mapper.readValue(licenseKey, LicenseDataDTO.class);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				LocalDate validUntil = null;

				if (StringUtils.hasText(licenseData.getValidUntil())) {
					validUntil = LocalDate.parse(licenseData.getValidUntil(), formatter);
				}

				if (macAddresses.contains(licenseData.getMacAddress())
						&& (validUntil == null || validUntil.isAfter(LocalDate.now()))) {
					return;
				}
			}


			// Trial mode: allow usage for 6 months after first purchase order is created.
			LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
			boolean hasOldPurchaseOrders = purchaseOrderRepository.existsByCreatedAtBefore(cutoff);

			if (hasOldPurchaseOrders) {
				log.warn("Free trial allows recording purchase orders for up to 6 months.");
				log.warn("License is not active or has expired; please apply for a new license from the developer to continue using the Central server.");
				SpringApplication.exit(appContext, () -> 0);
			}

		} catch (Exception e) {
			log.error("Unable to check license!");
			log.error(e.getMessage(), e);
			SpringApplication.exit(appContext, () -> -1);
		}
	}
}
