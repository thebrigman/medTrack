package org.example.fhirplay.repo;

import org.example.fhirplay.model.MedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<MedicationEntity, Long> {
    Optional<MedicationEntity> findByFhirId(String fhirId);
    void deleteByFhirId(String fhirId);
}
