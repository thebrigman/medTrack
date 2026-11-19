package org.example.fhirplay.controller;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.service.MedicationService;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MedicationController {

    private final MedicationService medicationService;
    private final FhirContext fhirContext;

    @Autowired
    public MedicationController(MedicationService medicationService, FhirContext fhirContext) {
        this.medicationService = medicationService;
        this.fhirContext = fhirContext;
    }

    @PostMapping("/medication")
    public String save(@RequestBody String medicationJson) {
        Medication medication = medicationService.saveMedication(medicationJson);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medication);
    }

    @GetMapping("/medication")
    public String getAllMedications() {
        return medicationService.getAllMedicationsAsFHIR();
    }

    @GetMapping("/medication/{fhirId}")
    public String getByFhirId(@PathVariable String fhirId) {
        Medication medication = medicationService.getMedicationByFhirId(fhirId);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medication);
    }

    @PutMapping("/medication")
    public String update(@RequestBody String fhirJson) {
        Medication medication = medicationService.update(fhirJson);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medication);
    }

    @DeleteMapping("/medication/{fhirId}")
    public void deleteByFhirId(@PathVariable String fhirId) {
        medicationService.deleteByFhirId(fhirId);
    }
}
