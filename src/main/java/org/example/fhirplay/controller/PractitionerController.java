package org.example.fhirplay.controller;

import org.example.fhirplay.service.PractitionerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PractitionerController {

    private final PractitionerService practitionerService;

    @Autowired
    public PractitionerController(PractitionerService practitionerService) {
        this.practitionerService = practitionerService;
    }

    @PostMapping("/practitioner")
    public String savePractitioner(@RequestBody String json) {
        return practitionerService.savePractitioner(json);
    }

    @GetMapping("/practitioner/{fhirId}")
    public String getPractitionerByFhirId(@PathVariable String fhirId) {
        return practitionerService.getPractitionerByFhirId(fhirId);
    }

    @GetMapping("/practitioner")
    public String getAllPractitioners() {
        return practitionerService.getAllPractitionersAsFHIR();
    }

    @DeleteMapping("practitioner/{fhirId}")
    public void deletePractitioner(@PathVariable String fhirId) {
        practitionerService.deletePractitionerByFhirId(fhirId);
    }

    @PutMapping("/practitioner")
    public String updatePractitioner(@RequestBody String fhirJson) {
        return practitionerService.updatePractitioner(fhirJson).getFhirJson();
    }
}
