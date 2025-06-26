package org.example.fhirplay.controller;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.service.PractitionerService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PractitionerController {

    private final PractitionerService practitionerService;
    private final FhirContext fhirContext;

    @Autowired
    public PractitionerController(PractitionerService practitionerService, FhirContext fhirContext) {
        this.practitionerService = practitionerService;
        this.fhirContext = fhirContext;
    }

    @PostMapping(
            value = "/practitioner",
            produces = "application/fhir+json"
    )
    public String savePractitioner(@RequestBody String json) {
        Practitioner practitioner = practitionerService.savePractitioner(json);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(practitioner);
    }

    @GetMapping(
            value = "/practitioner/{fhirId}",
            produces = "application/fhir+json")
    public String getPractitionerByFhirId(@PathVariable String fhirId) {
        Practitioner practitioner = practitionerService.getPractitionerByFhirId(fhirId);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(practitioner);
    }

    @GetMapping(
            value = "/practitioner",
            produces = "application/fhir+json"
    )
    public String getAllPractitioners() {
        Bundle bundle = practitionerService.getAllPractitionersAsFHIR();
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    @DeleteMapping(
            value = "practitioner/{fhirId}",
            produces = "application/fhir+json"
    )
    public String deletePractitioner(@PathVariable String fhirId) {
        Practitioner practitioner = practitionerService.deletePractitionerByFhirId(fhirId);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(practitioner);
    }

    @PutMapping(
            value = "/practitioner",
            produces = "application/fhir+json"
    )
    public String updatePractitioner(@RequestBody String fhirJson) {
        Practitioner practitioner = practitionerService.updatePractitioner(fhirJson);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(practitioner);
    }
}
