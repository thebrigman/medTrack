package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.mapper.FhirResourceToEntityMapper;
import org.example.fhirplay.model.PractitionerEntity;
import org.example.fhirplay.repo.PractitionerRepository;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PractitionerService {

    private final FhirContext fhirContext;
    private final PractitionerRepository practitionerRepository;
    private final FhirResourceToEntityMapper mapper;

    @Autowired
    public PractitionerService(PractitionerRepository practitionerRepository, FhirResourceToEntityMapper mapper) {
        this.fhirContext = FhirContext.forR4();
        this.practitionerRepository = practitionerRepository;
        this.mapper = mapper;
    }

    public String savePractitioner(String fhirJson) {
        Practitioner fhirPractitioner = fhirContext.newJsonParser().parseResource(Practitioner.class, fhirJson);

        if (practitionerRepository.existsByFhirId(fhirPractitioner.getIdElement().getIdPart())) {
            throw new IllegalArgumentException("FHIR ID " + fhirPractitioner.getIdElement().getIdPart() + " already exists.");
        }

        PractitionerEntity practitioner = mapper.toPractitionerEntity(fhirPractitioner);
        practitionerRepository.save(practitioner);

        return practitioner.getFhirJson();
    }

    public String getPractitionerByFhirId(String fhirId) {
        Optional<PractitionerEntity> practitioner = practitionerRepository.findByFhirId(fhirId);
        return practitioner.orElseThrow(() -> new NoSuchElementException("No such Practitioner with id of :" + fhirId)).getFhirJson();
    }

    public String getAllPractitionersAsFHIR() {
        List<PractitionerEntity> practitionerEntities = practitionerRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Add each Practitioner to the Bundle
        for (PractitionerEntity entity : practitionerEntities) {
            if(entity.getFhirJson() != null) {
                Practitioner practitioner = fhirContext.newJsonParser().parseResource(Practitioner.class, entity.getFhirJson());
                bundle.addEntry().setResource(practitioner);
            }

        }

        // Serialize the Bundle to JSON
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    @Transactional
    public void deletePractitionerByFhirId(String fhirId) {
        practitionerRepository.deletePractitionerEntityByFhirId(fhirId);
    }

    @Transactional
    public PractitionerEntity updatePractitioner(String fhirJson) {
        Practitioner updatedPractitioner = fhirContext.newJsonParser().parseResource(Practitioner.class, fhirJson);
        Optional<PractitionerEntity> practitionerEntityOptional = practitionerRepository.findByFhirId(updatedPractitioner.getIdPart());
        PractitionerEntity practitionerEntity = practitionerEntityOptional.orElseThrow(() -> new NoSuchElementException("No such Practitioner with id of :" + updatedPractitioner.getIdPart()));

        return mapper.mapPractitionerFields(updatedPractitioner, practitionerEntity);
    }
}
