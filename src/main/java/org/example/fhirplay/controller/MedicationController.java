package org.example.fhirplay.controller;

import org.example.fhirplay.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MedicationController {

    private final MedicationService medicationService;

    @Autowired
    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping("/medication")
    public String save(@RequestBody String medicationJson) {
        return medicationService.saveMedication(medicationJson);
    }

    @GetMapping("/medication")
    public String getAllMedications() {
        return medicationService.getAllMedicationsAsFHIR();
    }

    @GetMapping("/medication/{fhirId}")
    public String getByFhirId(@PathVariable String fhirId) {
        return medicationService.getMedicationByFhirId(fhirId);
    }

    @PutMapping("/medication")
    public String update(@RequestBody String fhirJson) {
        return medicationService.update(fhirJson);
    }

    @DeleteMapping("/medication/{fhirId}")
    public void deleteByFhirId(@PathVariable String fhirId) {
        medicationService.deleteByFhirId(fhirId);
    }
}
