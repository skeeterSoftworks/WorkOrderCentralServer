package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin UI payload for license activation tab.
 * {@code scenario}: {@code NONE}, {@code TIME_LIMITED}, {@code PERPETUAL}, or {@code ERROR}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseActivationInfoTO {

    private String scenario;
    private List<String> macAddresses = new ArrayList<>();
    private String issuedTo;
    /** As stored in the license JSON ({@code dd/MM/yyyy}). */
    private String validUntil;
    private Boolean timeLimitedExpired;
    /** MAC address bound in the license file. */
    private String licenseMacAddress;
    private Boolean macMatchesLicense;
    private String errorMessage;
}
