package org.example.fhirplay.controller;

import org.example.fhirplay.service.MedicationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MedicationRequestController {
    private final MedicationRequestService medicationRequestService;

    @Autowired
    public MedicationRequestController(MedicationRequestService medicationRequestService) {
        this.medicationRequestService = medicationRequestService;
    }

    @PostMapping("/medication-request")
    public String saveMedRequest(@RequestBody String json) {
        return medicationRequestService.saveMedRequest(json);
    }

    @GetMapping("/medication-requests")
    public String getMedicationRequestsInFhirFormat() {
        return medicationRequestService.getAllMedicationRequestsAsFHIR();
    }

    @GetMapping("/medication-request/{fhirId}")
    public String getByFhirId(@PathVariable String fhirId) {
        return medicationRequestService.findMedRequestByFhirId(fhirId).getFhirJson();
    }

    @DeleteMapping("medication-request/{fhirId}")
    public void deleteMedRequest(@PathVariable String fhirId) {
        medicationRequestService.deleteMedRequestByFhirId(fhirId);
    }

    @PutMapping("/medication-request")
    public String updateMedRequest(@RequestBody String fhirJson) {
        return medicationRequestService.updateMedRequest(fhirJson).getFhirJson();
    }
}
