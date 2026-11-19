package org.example.fhirplay.ui;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Backing object for the "new medication request" form.
 */
@Data
public class MedRequestForm {

    private String patientFhirId;

    private String medicationFhirId;

    /** Prescriber */
    private String practitionerFhirId;

    /** FHIR request-priority code: routine | urgent | asap | stat */
    private String priority;

    /** Free text, e.g. "Take 1 tablet every 6 hours as needed for pain" */
    private String dosageInstruction;

    /** Matches the value an <input type="date"> submits */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate authoredOn;
}
