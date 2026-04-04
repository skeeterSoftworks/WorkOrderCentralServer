package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.to.LicenseDataDTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.LicenseActivationInfoTO;
import com.skeeterSoftworks.WorkOrderCentral.util.LicenseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.net.SocketException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class LicenseActivationService {

    private static final DateTimeFormatter LICENSE_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${license.key:none}")
    private String licenseKey;

    public LicenseActivationInfoTO getActivationInfo() {
        LicenseActivationInfoTO to = new LicenseActivationInfoTO();
        try {
            List<String> macs = LicenseUtils.getMacAddresses();
            to.setMacAddresses(macs != null ? macs : List.of());
        } catch (SocketException e) {
            log.warn("Could not read MAC addresses: {}", e.getMessage());
            to.setMacAddresses(List.of());
        }

        String key = licenseKey != null ? licenseKey : "none";
        if ("none".equals(key)) {
            to.setScenario("NONE");
            return to;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            LicenseDataDTO data = mapper.readValue(key, LicenseDataDTO.class);
            to.setIssuedTo(data.getIssuedTo());
            to.setLicenseMacAddress(data.getMacAddress());
            boolean macOk = to.getMacAddresses() != null && data.getMacAddress() != null
                    && to.getMacAddresses().contains(data.getMacAddress());
            to.setMacMatchesLicense(macOk);

            if (StringUtils.hasText(data.getValidUntil())) {
                to.setScenario("TIME_LIMITED");
                to.setValidUntil(data.getValidUntil().trim());
                LocalDate until = LocalDate.parse(data.getValidUntil().trim(), LICENSE_DATE);
                to.setTimeLimitedExpired(until.isBefore(LocalDate.now()));
            } else {
                to.setScenario("PERPETUAL");
                to.setTimeLimitedExpired(null);
                to.setValidUntil(null);
            }
        } catch (Exception e) {
            log.error("Failed to parse license for activation info: {}", e.getMessage());
            to.setScenario("ERROR");
            to.setErrorMessage(e.getMessage());
        }
        return to;
    }
}
