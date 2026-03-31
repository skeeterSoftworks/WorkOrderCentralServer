package com.skeeterSoftworks.WorkOrderCentral.service;

public class MachineDeleteBlockedException extends Exception {

    private final int linkedProductCount;

    public MachineDeleteBlockedException(int linkedProductCount) {
        this.linkedProductCount = linkedProductCount;
    }

    public int getLinkedProductCount() {
        return linkedProductCount;
    }
}
