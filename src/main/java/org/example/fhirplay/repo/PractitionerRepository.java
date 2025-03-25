package org.example.fhirplay.repo;

import org.example.fhirplay.model.PractitionerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PractitionerRepository extends JpaRepository<PractitionerEntity, Long> {
    boolean existsByFhirId(String idPart);
    Optional<PractitionerEntity> findByFhirId(String practitionerFhirId);
    void deletePractitionerEntityByFhirId(String fhirId);
}
