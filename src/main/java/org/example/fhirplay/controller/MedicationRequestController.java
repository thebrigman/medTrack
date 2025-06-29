package org.example.fhirplay.controller;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.service.MedicationRequestService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MedicationRequestController {
    private final MedicationRequestService medicationRequestService;
    private final FhirContext fhirContext;

    @Autowired
    public MedicationRequestController(MedicationRequestService medicationRequestService, FhirContext fhirContext) {
        this.medicationRequestService = medicationRequestService;
        this.fhirContext = fhirContext;
    }

    @PostMapping("/medication-request")
    public String saveMedRequest(@RequestBody String json) {
        MedicationRequest medicationRequest = medicationRequestService.saveMedRequest(json);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medicationRequest);
    }

    @GetMapping(
            value = "/medication-requests",
            produces = "application/fhir+json"
    )
    public String getMedicationRequestsInFhirFormat() {
        Bundle bundle = medicationRequestService.getAllMedicationRequestsAsFHIR();
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    @GetMapping(
            value = "/medication-request/{fhirId}",
            produces = "application/fhir+json"
    )
    public String getByFhirId(@PathVariable String fhirId) {
        MedicationRequest medicationRequest = medicationRequestService.findMedRequestByFhirId(fhirId);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medicationRequest);
    }

    @DeleteMapping(
            value = "medication-request/{fhirId}",
            produces = "application/fhir+json"
    )
    public void deleteMedRequest(@PathVariable String fhirId) {
        medicationRequestService.deleteMedRequestByFhirId(fhirId);
    }

    @PutMapping(
            value = "/medication-request",
            produces = "application/fhir+json"
    )
    public String updateMedRequest(@RequestBody String fhirJson) {
        MedicationRequest medicationRequest =  medicationRequestService.updateMedRequest(fhirJson);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medicationRequest);
    }
}
