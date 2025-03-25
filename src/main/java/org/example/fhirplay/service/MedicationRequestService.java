package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
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
    private final FhirResourceToEntityMapper mapper;

    @Autowired
    public MedicationRequestService(MedicationRequestRepository medicationRequestRepository,
                                    FhirResourceToEntityMapper mapper) {
        this.fhirContext = FhirContext.forR4();
        this.medicationRequestRepository = medicationRequestRepository;
        this.mapper = mapper;
    }

    public String getAllMedicationRequestsAsFHIR() {
        List<MedicationRequestEntity> medRequestEntities = medicationRequestRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        //Add medication requests to bundle
        for (MedicationRequestEntity medRequestEntity : medRequestEntities) {
            MedicationRequest medicationRequest = fhirContext.newJsonParser().parseResource(MedicationRequest.class, medRequestEntity.getFhirJson());
            bundle.addEntry().setResource(medicationRequest);
        }

        // Serialize the Bundle to JSON
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    public MedicationRequestEntity findMedRequestByFhirId(String fhirId) {
        Optional<MedicationRequestEntity> medicationRequest = medicationRequestRepository.findByFhirId(fhirId);
        return medicationRequest.orElseThrow(() -> new NoSuchElementException("No such medication request with FhirId of " + fhirId + " found."));
    }

    public String saveMedRequest(String fhirJson) {
        MedicationRequest medicationRequest = fhirContext.newJsonParser().parseResource(MedicationRequest.class, fhirJson);
        MedicationRequestEntity entity = mapper.toMedRequestEntity(medicationRequest);
        medicationRequestRepository.save(entity);
        return entity.getFhirJson();
    }

    @Transactional
    public MedicationRequestEntity updateMedRequest(String fhirJson) {
        MedicationRequest updatedRequest = fhirContext.newJsonParser().parseResource(MedicationRequest.class, fhirJson);
        Optional<MedicationRequestEntity> existingRequestOp = medicationRequestRepository.findByFhirId(updatedRequest.getIdPart());
        MedicationRequestEntity existingRequest = existingRequestOp.orElseThrow(() -> new NoSuchElementException("No such medication request with FhirId of " + updatedRequest.getIdPart() + " found."));

        return mapper.setMedicationRequestFields(updatedRequest, existingRequest);
    }

    @Transactional
    public void deleteMedRequestByFhirId(String fhirId) {
        medicationRequestRepository.deleteMedicationRequestEntityByFhirId(fhirId);
    }
}
