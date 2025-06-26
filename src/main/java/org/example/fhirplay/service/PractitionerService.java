package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.mapper.EnitiyToFhirResourceMapper;
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
    private final FhirResourceToEntityMapper toEntityMapper;
    private final EnitiyToFhirResourceMapper toFhirResourceMapper;

    @Autowired
    public PractitionerService(PractitionerRepository practitionerRepository, FhirResourceToEntityMapper toEntityMapper, EnitiyToFhirResourceMapper toFhirResourceMapper) {
        this.toFhirResourceMapper = toFhirResourceMapper;
        this.fhirContext = FhirContext.forR4();
        this.practitionerRepository = practitionerRepository;
        this.toEntityMapper = toEntityMapper;
    }

    public Practitioner savePractitioner(String fhirJson) {
        Practitioner fhirPractitioner = fhirContext.newJsonParser().parseResource(Practitioner.class, fhirJson);

        if (practitionerRepository.existsByFhirId(fhirPractitioner.getIdElement().getIdPart())) {
            throw new IllegalArgumentException("FHIR ID " + fhirPractitioner.getIdElement().getIdPart() + " already exists.");
        }

        PractitionerEntity entity = toEntityMapper.toPractitionerEntity(fhirPractitioner);
        practitionerRepository.save(entity);

        return toFhirResourceMapper.toFhirPractitioner(entity);
    }

    public Practitioner getPractitionerByFhirId(String fhirId) {
        Optional<PractitionerEntity> entityOptional = practitionerRepository.findByFhirId(fhirId);
        PractitionerEntity entity = entityOptional.orElseThrow(() -> new NoSuchElementException("No such Practitioner with id of :" + fhirId));
        return toFhirResourceMapper.toFhirPractitioner(entity);
    }

    public Bundle getAllPractitionersAsFHIR() {
        List<PractitionerEntity> practitionerEntities = practitionerRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Add each Practitioner to the Bundle
        for (PractitionerEntity entity : practitionerEntities) {
            Practitioner practitioner = toFhirResourceMapper.toFhirPractitioner(entity);
            bundle.addEntry().setResource(practitioner);
        }

        // Serialize the Bundle to JSON
        return bundle;
    }

    @Transactional
    public Practitioner deletePractitionerByFhirId(String fhirId) {
        Optional<PractitionerEntity> entityOptional = practitionerRepository.findByFhirId(fhirId);
        PractitionerEntity entity = entityOptional.orElseThrow(() -> new NoSuchElementException("No such Practitioner with id of :" + fhirId + " found."));
        Practitioner practitioner = toFhirResourceMapper.toFhirPractitioner(entity);
        practitionerRepository.deletePractitionerEntityByFhirId(fhirId);
        return practitioner;

    }

    @Transactional
    public Practitioner updatePractitioner(String fhirJson) {
        Practitioner updatedPractitioner = fhirContext.newJsonParser().parseResource(Practitioner.class, fhirJson);
        Optional<PractitionerEntity> practitionerEntityOptional = practitionerRepository.findByFhirId(updatedPractitioner.getIdPart());
        PractitionerEntity practitionerEntity = practitionerEntityOptional.orElseThrow(() -> new NoSuchElementException("No such Practitioner with id of :" + updatedPractitioner.getIdPart()));
        toEntityMapper.mapPractitionerFields(updatedPractitioner, practitionerEntity);
        return toFhirResourceMapper.toFhirPractitioner(practitionerEntity);
    }
}
