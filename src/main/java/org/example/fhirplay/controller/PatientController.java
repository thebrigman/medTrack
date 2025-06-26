package org.example.fhirplay.controller;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.service.PatientService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PatientController {
    private final PatientService patientService;
    private final FhirContext fhirContext;

    @Autowired
    public PatientController(PatientService patientService, FhirContext fhirContext) {
        this.patientService = patientService;
        this.fhirContext = fhirContext;
    }

    @GetMapping(
            value = "/patients",
            produces = "application/fhir+json"
    )
    public String getAllPatients() {
        Bundle bundle = patientService.getAllAsFHIR();
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    @PostMapping(
            value = "/patients",
            produces = "application/fhir+json"
    )
    public String savePatient(@RequestBody String patientJson) {
        Patient patient = patientService.save(patientJson);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
    }

    @GetMapping(
    value    = "/patients/{fhirId}",
    produces = "application/fhir+json"
    )
    public String getByFhirId(@PathVariable String fhirId) {
        Patient patient = patientService.getByFhirId(fhirId);
        return fhirContext.newJsonParser().encodeResourceToString(patient);
    }

    @DeleteMapping(
            value    = "/patients/{fhirId}",
            produces = "application/fhir+json"
    )
    public String deleteByFhirId(@PathVariable String fhirId) {
        Patient patient = patientService.deleteByFhirId(fhirId);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
    }

    @PutMapping(
            value    = "/patients",
            produces = "application/fhir+json"
    )
    public String updatePatient(@RequestBody String json) {
        Patient patient = patientService.update(json);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
    }
}


