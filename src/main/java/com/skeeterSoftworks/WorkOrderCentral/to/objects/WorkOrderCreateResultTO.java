package com.skeeterSoftworks.WorkOrderCentral.to.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderCreateResultTO {
    private WorkOrderTO workOrder;
    /** Base64-encoded PDF when stock was assigned from locations; null otherwise. */
    private String stockAssignmentOrderPdfBase64;
    /** Base64-encoded material requirements PDF generated on work order create. */
    private String materialRequirementsPdfBase64;
}
