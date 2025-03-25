package org.example.fhirplay.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "medication_administration")
public class MedicationAdministrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fhir_id", unique = true, nullable = false)
    private String fhirId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name = "medication_id", nullable = false)
    private MedicationEntity medication;

    @Column(name = "status")
    private String status;

    @Column(name = "effective_date_time")
    private String effectiveDateTime;

    @Column(name = "dosage_quantity")
    private Double dosageQuantity;

    @Column(name = "dosage_unit")
    private String dosageUnit;

    @Column(name = "dosage_route")
    private String dosageRoute;

    @Column(name = "fhir_json", columnDefinition = "TEXT", nullable = false)
    private String fhirJson;
}

