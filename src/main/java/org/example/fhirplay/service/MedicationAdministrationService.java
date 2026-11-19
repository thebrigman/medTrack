package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.mapper.EntityToFhirResourceMapper;
import org.example.fhirplay.mapper.FhirResourceToEntityMapper;
import org.example.fhirplay.model.MedicationAdministrationEntity;
import org.example.fhirplay.repo.MedicationAdministrationRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MedicationAdministrationService {

    private final FhirContext fhirContext;
    private final MedicationAdministrationRepository medicationAdministrationRepository;
    private final FhirResourceToEntityMapper toEntityMapper;
    private final EntityToFhirResourceMapper toFhirResourceMapper;

    @Autowired
    public MedicationAdministrationService(MedicationAdministrationRepository medicationAdministrationRepository,
                                           FhirResourceToEntityMapper toEntityMapper, EntityToFhirResourceMapper toFhirResourceMapper, FhirContext fhirContext) {
        this.fhirContext = fhirContext;
        this.medicationAdministrationRepository = medicationAdministrationRepository;
        this.toEntityMapper = toEntityMapper;
        this.toFhirResourceMapper = toFhirResourceMapper;
    }

    public MedicationAdministration saveMedicationAdmin(String fhirJson) {
        MedicationAdministration medicationAdministration = fhirContext.newJsonParser().parseResource(MedicationAdministration.class, fhirJson);
        return saveMedicationAdmin(medicationAdministration);
    }

    // Used by the UI, which builds the FHIR resource directly instead of parsing JSON
    @Transactional
    public MedicationAdministration saveMedicationAdmin(MedicationAdministration medicationAdministration) {
        MedicationAdministrationEntity entity = toEntityMapper.toMedicationAdminEntity(medicationAdministration);
        medicationAdministrationRepository.save(entity);
        return toFhirResourceMapper.toFhirMedicationAdministration(entity);
    }

    public String getAllMedicationAdministrationsAsFHIR() {
        List<MedicationAdministrationEntity> medAdminEntities = medicationAdministrationRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Add each MedicationAdministration to the Bundle
        for (MedicationAdministrationEntity entity : medAdminEntities) {
            MedicationAdministration medAdmin = toFhirResourceMapper.toFhirMedicationAdministration(entity);
            bundle.addEntry().setResource(medAdmin);
        }

        // Serialize the Bundle to JSON
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    public MedicationAdministration getMedicationAdminByFhirId(String fhirId) {
        Optional<MedicationAdministrationEntity> entityOptional = medicationAdministrationRepository.findByFhirId(fhirId);
        MedicationAdministrationEntity entity = entityOptional.orElseThrow(() -> new NoSuchElementException("No such Medication Administration with Id of: " + fhirId));
        return toFhirResourceMapper.toFhirMedicationAdministration(entity);
    }

    @Transactional
    public void deleteMedicationAdministrationByFhirId(String fhirId) {
        medicationAdministrationRepository.deleteByFhirId(fhirId);
    }

    @Transactional
    public MedicationAdministration updateMedicationAdministration(String fhirJson) {
        MedicationAdministration updatedMedAdmin = fhirContext.newJsonParser().parseResource(MedicationAdministration.class, fhirJson);
        Optional<MedicationAdministrationEntity>  existingMedAdminOptional = medicationAdministrationRepository.findByFhirId(updatedMedAdmin.getIdPart());
        MedicationAdministrationEntity existingEntity = existingMedAdminOptional.orElseThrow(() -> new NoSuchElementException("No such Medication Administration with Id of: " + updatedMedAdmin.getIdPart()));

        MedicationAdministrationEntity entity = toEntityMapper.setMedicationAdministrationEntityFields(updatedMedAdmin, existingEntity);
        medicationAdministrationRepository.save(entity);

        return toFhirResourceMapper.toFhirMedicationAdministration(existingEntity);
    }
}
