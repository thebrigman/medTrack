package org.example.fhirplay.controller;

import org.example.fhirplay.service.MedicationAdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MedicationAdministrationController {

    private final MedicationAdministrationService medicationAdministrationService;

    @Autowired
    public MedicationAdministrationController(MedicationAdministrationService medicationAdministrationService) {
        this.medicationAdministrationService = medicationAdministrationService;
    }

    @PostMapping("medication-administration")
    public String save(@RequestBody String json) {
       return medicationAdministrationService.saveMedicationAdmin(json);
    }

    @GetMapping("medication-administration/{fhirId}")
    public String getByFhirId(@PathVariable String fhirId) {
        return medicationAdministrationService.getMedicationAdminByFhirId(fhirId);
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
        return medicationAdministrationService.updateMedicationAdministration(fhirJson);
    }

}
