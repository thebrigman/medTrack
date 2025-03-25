package org.example.fhirplay.controller;

import org.example.fhirplay.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PatientController {
    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patient")
    public String getAllPatients() {
        return patientService.getAllPatientsAsFHIR();
    }

    @PostMapping("/patient")
    public String savePatient(@RequestBody String patientJson) {
        return patientService.savePatient(patientJson);
    }

    @GetMapping("patient/{fhirId}")
    public String getByFhirId(@PathVariable String fhirId) {
        return patientService.getPatientByFhirId(fhirId).getFhirJson();
    }

    @DeleteMapping("/patient/{fhirId}")
    public String deleteByFhirId(@PathVariable String fhirId) {
        return patientService.deletePatientByFhirId(fhirId);
    }

    @PutMapping("/patient")
    public String updatePatient(@RequestBody String json) {
        return patientService.updatePatient(json).getFhirJson();
    }
}


