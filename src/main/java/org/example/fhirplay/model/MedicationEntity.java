package org.example.fhirplay.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "medication")
public class MedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "medication_name")
    private String medicationName;

    @Column(name = "form")
    private String form;

    @Column(name = "strength_value")
    private Double strengthValue;

    @Column(name = "strength_unit")
    private String strengthUnit;

    @Column(name = "fhir_id", unique = true, nullable = false)
    private String fhirId;

    @Column(name = "fhir_json", columnDefinition = "TEXT", nullable = false)
    private String fhirJson;
}
