package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.mapper.FhirResourceToEntityMapper;
import org.example.fhirplay.repo.MedicationRepository;
import org.example.fhirplay.model.MedicationEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MedicationService {

    private final FhirContext fhirContext;
    private final FhirResourceToEntityMapper mapper;
    private final MedicationRepository medicationRepository;

    @Autowired
    public MedicationService(FhirResourceToEntityMapper mapper, MedicationRepository medicationRepository) {
        this.fhirContext = FhirContext.forR4();
        this.mapper = mapper;
        this.medicationRepository = medicationRepository;
    }


    public String saveMedication(String fhirMedicationJson) {
        Medication medication = fhirContext.newJsonParser().parseResource(Medication.class, fhirMedicationJson);
        MedicationEntity entity = mapper.toMedicationEntity(medication);
        medicationRepository.save(entity);
        return entity.getFhirJson();
    }

    public String getAllMedicationsAsFHIR() {
        List<MedicationEntity> medicationEntities = medicationRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Add each Medication to the Bundle
        for (MedicationEntity entity : medicationEntities) {
            Medication medication = fhirContext.newJsonParser().parseResource(Medication.class, entity.getFhirJson());
            bundle.addEntry().setResource(medication);
        }

        // Serialize the Bundle to JSON
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    public String getMedicationByFhirId(String fhirId) {
        Optional<MedicationEntity> medicationEntity = medicationRepository.findByFhirId(fhirId);
        return medicationEntity.orElseThrow(() -> new NoSuchElementException("No such medication with id of: " + fhirId)).getFhirJson();
    }

    @Transactional
    public void deleteByFhirId(String fhirId) {
        medicationRepository.deleteByFhirId(fhirId);
    }

    @Transactional
    public String update(String fhirJson) {
        Medication updatedMedication = fhirContext.newJsonParser().parseResource(Medication.class, fhirJson);
        Optional<MedicationEntity> existingEntityOptional = medicationRepository.findByFhirId(updatedMedication.getIdPart());
        MedicationEntity existingMedication = existingEntityOptional.orElseThrow(() -> new NoSuchElementException("No such medication with id of: " + updatedMedication.getIdPart()));

        return mapper.setMedicationFields(updatedMedication, existingMedication).getFhirJson();
    }
}
