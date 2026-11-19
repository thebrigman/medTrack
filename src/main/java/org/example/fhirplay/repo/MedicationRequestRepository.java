package org.example.fhirplay.repo;

import org.example.fhirplay.model.MedicationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationRequestRepository extends JpaRepository<MedicationRequestEntity, Long> {
    Optional<MedicationRequestEntity> findByFhirId(String fhirId);
    List<MedicationRequestEntity> findMedicationRequestEntitiesByPatient_FhirId(String PatientFhirId);
    void deleteMedicationRequestEntityByFhirId(String fhirId);

    // UI: requests a nurse can act on (e.g. status "active"), newest first
    List<MedicationRequestEntity> findByStatusOrderByAuthoredOnDesc(String status);
}
