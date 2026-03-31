package com.skeeterSoftworks.WorkOrderCentral.service;

/**
 * Thrown when a catalog product cannot be deleted because it is still referenced
 * (e.g. on purchase order lines).
 */
public class ProductDeleteBlockedException extends Exception {

    private final int productOrderLineCount;

    public ProductDeleteBlockedException(int productOrderLineCount) {
        this.productOrderLineCount = productOrderLineCount;
    }

    public int getProductOrderLineCount() {
        return productOrderLineCount;
    }
}
