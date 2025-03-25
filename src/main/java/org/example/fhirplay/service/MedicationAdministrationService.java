package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
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
    private final FhirResourceToEntityMapper mapper;

    @Autowired
    public MedicationAdministrationService(MedicationAdministrationRepository medicationAdministrationRepository,
                                           FhirResourceToEntityMapper mapper) {
        this.fhirContext = FhirContext.forR4();
        this.medicationAdministrationRepository = medicationAdministrationRepository;
        this.mapper = mapper;
    }

    public String saveMedicationAdmin(String fhirJson) {
        MedicationAdministration medicationAdministration = fhirContext.newJsonParser().parseResource(MedicationAdministration.class, fhirJson);
        MedicationAdministrationEntity entity = mapper.toMedicationAdminEntity(medicationAdministration);
        medicationAdministrationRepository.save(entity);
        return entity.getFhirJson();
    }

    public String getAllMedicationAdministrationsAsFHIR() {
        List<MedicationAdministrationEntity> medAdminEntities = medicationAdministrationRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Add each MedicationAdministration to the Bundle
        for (MedicationAdministrationEntity entity : medAdminEntities) {
            MedicationAdministration medAdmin = fhirContext.newJsonParser().parseResource(MedicationAdministration.class, entity.getFhirJson());
            bundle.addEntry().setResource(medAdmin);
        }

        // Serialize the Bundle to JSON
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    public String getMedicationAdminByFhirId(String fhirId) {
        Optional<MedicationAdministrationEntity> entity = medicationAdministrationRepository.findByFhirId(fhirId);
        return entity.orElseThrow(() -> new NoSuchElementException("No such Medication Administration with Id of: " + fhirId)).getFhirJson();
    }

    @Transactional
    public void deleteMedicationAdministrationByFhirId(String fhirId) {
        medicationAdministrationRepository.deleteByFhirId(fhirId);
    }

    @Transactional
    public String updateMedicationAdministration(String fhirJson) {
        MedicationAdministration updatedMedAdmin = fhirContext.newJsonParser().parseResource(MedicationAdministration.class, fhirJson);
        Optional<MedicationAdministrationEntity>  existingMedAdminOptional = medicationAdministrationRepository.findByFhirId(updatedMedAdmin.getIdPart());
        MedicationAdministrationEntity existingEntity = existingMedAdminOptional.orElseThrow(() -> new NoSuchElementException("No such Medication Administration with Id of: " + updatedMedAdmin.getIdPart()));

        return mapper.setMedicationAdministrationEntityFields(updatedMedAdmin, existingEntity).getFhirJson();
    }
}
