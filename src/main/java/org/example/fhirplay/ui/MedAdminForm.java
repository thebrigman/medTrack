package org.example.fhirplay.ui;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Backing object for the "log administration" form.
 * Patient and medication come from the MedicationRequest in the URL, so the user can't change them.
 */
@Data
public class MedAdminForm {

    private String practitionerFhirId;

    private Double dosageQuantity;

    private String dosageUnit;

    /** SNOMED CT code of the route, one of GuiController.ROUTES */
    private String routeCode;

    /** Matches the value an <input type="datetime-local"> submits */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime effectiveDateTime;
}
