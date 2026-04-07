package com.skeeterSoftworks.WorkOrderCentral.service;

public class MachineDeleteBlockedByBookingsException extends Exception {

    private final int bookingCount;

    public MachineDeleteBlockedByBookingsException(int bookingCount) {
        this.bookingCount = bookingCount;
    }

    public int getBookingCount() {
        return bookingCount;
    }
}
