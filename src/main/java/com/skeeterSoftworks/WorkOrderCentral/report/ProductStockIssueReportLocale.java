package com.skeeterSoftworks.WorkOrderCentral.report;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Component
public class ProductStockIssueReportLocale {

    private static final String BUNDLE_BASE = "reports/product-stock-issue";

    @Value("${reports.product-stock-issue.locale:sr}")
    private String configuredLocale;

    public String get(String key) {
        try {
            return bundle().getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public DateTimeFormatter issuedAtFormatter() {
        return "en".equalsIgnoreCase(normalizedLocale())
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                : DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    }

    private ResourceBundle bundle() {
        return ResourceBundle.getBundle(BUNDLE_BASE, locale());
    }

    private Locale locale() {
        return Locale.forLanguageTag(normalizedLocale());
    }

    private String normalizedLocale() {
        if (configuredLocale == null || configuredLocale.isBlank()) {
            return "sr";
        }
        String trimmed = configuredLocale.trim().toLowerCase(Locale.ROOT);
        if (trimmed.startsWith("en")) {
            return "en";
        }
        return "sr";
    }
}
