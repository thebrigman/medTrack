package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.mapper.EntityToFhirResourceMapper;
import org.example.fhirplay.mapper.FhirResourceToEntityMapper;
import org.example.fhirplay.model.MedicationRequestEntity;
import org.example.fhirplay.repo.MedicationRequestRepository;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MedicationRequestService {

    private final FhirContext fhirContext;
    private final MedicationRequestRepository medicationRequestRepository;
    private final FhirResourceToEntityMapper toEntityMapper;
    private final EntityToFhirResourceMapper toFhirResourceMapper;

    @Autowired
    public MedicationRequestService(MedicationRequestRepository medicationRequestRepository,
                                    FhirResourceToEntityMapper toEntityMapper, EntityToFhirResourceMapper toFhirResourceMapper, FhirContext fhirContext) {
        this.toFhirResourceMapper = toFhirResourceMapper;
        this.fhirContext = fhirContext;
        this.medicationRequestRepository = medicationRequestRepository;
        this.toEntityMapper = toEntityMapper;
    }

    public Bundle getAllMedicationRequestsAsFHIR() {
        List<MedicationRequestEntity> medRequestEntities = medicationRequestRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        //Add medication requests to bundle
        for (MedicationRequestEntity medRequestEntity : medRequestEntities) {
            MedicationRequest medicationRequest = toFhirResourceMapper.toFhirMedicationRequest(medRequestEntity);
            bundle.addEntry().setResource(medicationRequest);
        }

        // Serialize the Bundle to JSON
        return bundle;
    }

    public MedicationRequest findMedRequestByFhirId(String fhirId) {
        Optional<MedicationRequestEntity> entityOptional = medicationRequestRepository.findByFhirId(fhirId);
        MedicationRequestEntity entity = entityOptional.orElseThrow(() -> new NoSuchElementException("No such medication request with FhirId of " + fhirId + " found."));
        return toFhirResourceMapper.toFhirMedicationRequest(entity);
    }

    public MedicationRequest saveMedRequest(String fhirJson) {
        MedicationRequest medicationRequest = fhirContext.newJsonParser().parseResource(MedicationRequest.class, fhirJson);
        return saveMedRequest(medicationRequest);
    }

    // Used by the UI, which builds the FHIR resource directly instead of parsing JSON
    @Transactional
    public MedicationRequest saveMedRequest(MedicationRequest medicationRequest) {
        MedicationRequestEntity entity = toEntityMapper.toMedRequestEntity(medicationRequest);
        medicationRequestRepository.save(entity);

        return toFhirResourceMapper.toFhirMedicationRequest(entity);
    }

    @Transactional
    public MedicationRequest updateMedRequest(String fhirJson) {
        MedicationRequest updatedRequest = fhirContext.newJsonParser().parseResource(MedicationRequest.class, fhirJson);
        Optional<MedicationRequestEntity> existingRequestOp = medicationRequestRepository.findByFhirId(updatedRequest.getIdPart());
        MedicationRequestEntity existingRequest = existingRequestOp.orElseThrow(() -> new NoSuchElementException("No such medication request with FhirId of " + updatedRequest.getIdPart() + " found."));

        MedicationRequestEntity entity = toEntityMapper.setMedicationRequestFields(updatedRequest, existingRequest);
        return toFhirResourceMapper.toFhirMedicationRequest(entity);
    }

    @Transactional
    public void deleteMedRequestByFhirId(String fhirId) {
        medicationRequestRepository.deleteMedicationRequestEntityByFhirId(fhirId);
    }
}
