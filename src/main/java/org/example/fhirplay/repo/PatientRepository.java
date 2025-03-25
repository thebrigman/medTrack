package org.example.fhirplay.repo;

import org.example.fhirplay.model.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    Optional<PatientEntity> findByFhirId(String fhirId);
    void deleteByFhirId(String fhirId);
}
