package com.skeeterSoftworks.WorkOrderCentral.util;

import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialOrderReceptionTO;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MaterialReceptionStockAllocationTO;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ColorMarkerUtil {

    private static final Set<String> ALLOWED = Set.of(
            "RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "PURPLE", "PINK", "BROWN", "GRAY", "BLACK");

    private ColorMarkerUtil() {
    }

    public static String normalizeOrNull(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return ALLOWED.contains(normalized) ? normalized : null;
    }

    /**
     * Prefers top-level reception color, otherwise first non-empty allocation color.
     */
    public static String resolveFromReception(MaterialOrderReceptionTO to) {
        if (to == null) {
            return null;
        }
        String fromTop = normalizeOrNull(to.getColorMarker());
        if (fromTop != null) {
            return fromTop;
        }
        List<MaterialReceptionStockAllocationTO> allocations = to.getStockAllocations();
        if (allocations == null) {
            return null;
        }
        for (MaterialReceptionStockAllocationTO allocation : allocations) {
            if (allocation == null) {
                continue;
            }
            String fromRow = normalizeOrNull(allocation.getColorMarker());
            if (fromRow != null) {
                return fromRow;
            }
        }
        return null;
    }
}
