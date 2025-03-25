package org.example.fhirplay.repo;

import org.example.fhirplay.model.MedicationAdministrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministrationEntity, Long> {
    List<MedicationAdministrationEntity> findMedicationAdministrationEntitiesByPatient_FhirId(String patient_fhirId);
    Optional<MedicationAdministrationEntity> findByFhirId(String fhirId);
    void deleteByFhirId(String fhirId);
}
