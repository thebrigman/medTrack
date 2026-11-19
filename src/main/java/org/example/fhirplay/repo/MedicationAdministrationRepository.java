package org.example.fhirplay.repo;

import org.example.fhirplay.model.MedicationAdministrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministrationEntity, Long> {
    List<MedicationAdministrationEntity> findMedicationAdministrationEntitiesByPatient_FhirId(String patient_fhirId);
    Optional<MedicationAdministrationEntity> findByFhirId(String fhirId);
    void deleteByFhirId(String fhirId);

    // UI: earlier doses of the same medication for the same patient, newest first
    List<MedicationAdministrationEntity> findByPatient_FhirIdAndMedication_FhirIdOrderByEffectiveDateTimeDesc(String patientFhirId, String medicationFhirId);

    // UI: most recent administrations across all patients
    List<MedicationAdministrationEntity> findTop50ByOrderByEffectiveDateTimeDesc();
}
