package org.example.fhirplay.controller;

import org.example.fhirplay.service.PatientFHIRBundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PatientBundleController {

    private final PatientFHIRBundleService fhirBundleService;

    @Autowired
    public PatientBundleController(PatientFHIRBundleService fhirBundleService) {
        this.fhirBundleService = fhirBundleService;
    }

    @GetMapping("/patient_bundle/{fhirId}")
    public String getPatientBundle(@PathVariable String fhirId) {
        return fhirBundleService.getPatientBundle(fhirId);
    }
}
