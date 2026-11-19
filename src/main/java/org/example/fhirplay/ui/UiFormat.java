package org.example.fhirplay.ui;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Display helpers for the Thymeleaf templates, called as ${@fmt.num(x)} and ${@fmt.time(x)}.
 */
@Component("fmt")
public class UiFormat {

    /** 200.0 -> "200", 2.5 -> "2.5", null -> "" */
    public String num(Double value) {
        if (value == null) return "";
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    /** "2025-03-10T08:30:00-05:00" -> "2025-03-10 08:30"; anything shorter is returned unchanged */
    public String time(String fhirDateTime) {
        if (fhirDateTime == null || fhirDateTime.isBlank()) return "—";
        if (fhirDateTime.length() < 16) return fhirDateTime;
        return fhirDateTime.substring(0, 16).replace('T', ' ');
    }
}
