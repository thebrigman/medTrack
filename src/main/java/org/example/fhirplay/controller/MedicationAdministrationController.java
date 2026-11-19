package org.example.fhirplay.controller;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.service.MedicationAdministrationService;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MedicationAdministrationController {

    private final MedicationAdministrationService medicationAdministrationService;
    private final FhirContext fhirContext;

    @Autowired
    public MedicationAdministrationController(MedicationAdministrationService medicationAdministrationService, FhirContext fhirContext) {
        this.medicationAdministrationService = medicationAdministrationService;
        this.fhirContext = fhirContext;
    }

    @PostMapping("medication-administration")
    public String save(@RequestBody String json) {
        MedicationAdministration medicationAdministration = medicationAdministrationService.saveMedicationAdmin(json);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medicationAdministration);
    }

    @GetMapping("medication-administration/{fhirId}")
    public String getByFhirId(@PathVariable String fhirId) {
        MedicationAdministration medicationAdministration = medicationAdministrationService.getMedicationAdminByFhirId(fhirId);
        return fhirContext.newJsonParser().encodeResourceToString(medicationAdministration);
    }

    @GetMapping("/medication-administration")
    public String getAllMedAdmins() {
        return medicationAdministrationService.getAllMedicationAdministrationsAsFHIR();
    }

    @DeleteMapping("/medication-administration/{fhirId}")
    public void deleteByFhirId(@PathVariable String fhirId) {
        medicationAdministrationService.deleteMedicationAdministrationByFhirId(fhirId);
    }

    @PutMapping("/medication-administration")
    public String updateMedAdmin(@RequestBody String fhirJson) {
        MedicationAdministration medicationAdministration = medicationAdministrationService.updateMedicationAdministration(fhirJson);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medicationAdministration);
    }

}
