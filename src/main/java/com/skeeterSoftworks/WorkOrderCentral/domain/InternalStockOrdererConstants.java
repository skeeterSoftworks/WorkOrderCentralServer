package com.skeeterSoftworks.WorkOrderCentral.domain;

/**
 * Canonical identity for the internal stock-building orderer (purchase orders, product links).
 */
public final class InternalStockOrdererConstants {

    /** Serbian display / DB value for the preset internal-stock customer. */
    public static final String COMPANY_NAME = "Interni radni nalog (za magacin)";

    /** Previous English name; migrated to {@link #COMPANY_NAME} when {@link com.skeeterSoftworks.WorkOrderCentral.service.CustomerService#ensureInternalStockOrdererCustomerExists()} runs. */
    public static final String LEGACY_COMPANY_NAME_EN = "Internal Work Order (for Stock)";

    private InternalStockOrdererConstants() {
    }
}
