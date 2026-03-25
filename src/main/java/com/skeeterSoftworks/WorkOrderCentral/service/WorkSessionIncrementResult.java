package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.WorkSession;

public record WorkSessionIncrementResult(WorkSession session, boolean workOrderCompletedByTarget) {
}
