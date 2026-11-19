package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.mapper.EntityToFhirResourceMapper;
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
    private final EntityToFhirResourceMapper toFhirResourceMapper;


    @Autowired
    public MedicationService(FhirResourceToEntityMapper mapper, MedicationRepository medicationRepository, EntityToFhirResourceMapper toFhirResourceMapper, FhirContext fhirContext) {
        this.toFhirResourceMapper = toFhirResourceMapper;
        this.fhirContext = fhirContext;
        this.mapper = mapper;
        this.medicationRepository = medicationRepository;
    }


    public Medication saveMedication(String fhirMedicationJson) {
        Medication medication = fhirContext.newJsonParser().parseResource(Medication.class, fhirMedicationJson);
        System.out.println("+++++++++++++++++++++++++++++++++\n MedicationService.saveMedication() Medication name: " + medication.getCode().getText());

        MedicationEntity entity = mapper.toMedicationEntity(medication);
        System.out.println("+++++++++++++++++++++++++++++++++\n MedicationService.saveMedication() entity name: " + entity.getMedicationName());
        medicationRepository.save(entity);
        return toFhirResourceMapper.toFhirMedication(entity);
    }

    public String getAllMedicationsAsFHIR() {
        List<MedicationEntity> medicationEntities = medicationRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Add each Medication to the Bundle
        for (MedicationEntity entity : medicationEntities) {
            Medication medication = toFhirResourceMapper.toFhirMedication(entity);
            bundle.addEntry().setResource(medication);
        }

        // Serialize the Bundle to JSON
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    public Medication getMedicationByFhirId(String fhirId) {
        Optional<MedicationEntity> medicationEntityOptional = medicationRepository.findByFhirId(fhirId);
        MedicationEntity entity = medicationEntityOptional.orElseThrow(() -> new NoSuchElementException("No such medication with id of: " + fhirId));
        return toFhirResourceMapper.toFhirMedication(entity);
    }

    @Transactional
    public void deleteByFhirId(String fhirId) {
        medicationRepository.deleteByFhirId(fhirId);
    }

    @Transactional
    public Medication update(String fhirJson) {
        Medication updatedMedication = fhirContext.newJsonParser().parseResource(Medication.class, fhirJson);
        Optional<MedicationEntity> existingEntityOptional = medicationRepository.findByFhirId(updatedMedication.getIdPart());
        MedicationEntity existingMedication = existingEntityOptional.orElseThrow(() -> new NoSuchElementException("No such medication with id of: " + updatedMedication.getIdPart()));
        MedicationEntity entity = mapper.setMedicationFields(updatedMedication, existingMedication);
        return toFhirResourceMapper.toFhirMedication(entity);
    }
}
