package org.example.fhirplay.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "medication_request")
public class MedicationRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name = "practitioner_id", nullable = false)
    private PractitionerEntity practitioner;

    @ManyToOne
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication;

    @Column(name = "fhir_id", unique = true, nullable = false)
    private String fhirId;

    @Column(name = "status")
    private String status;

    @Column(name = "intent")
    private String intent;

    @Column(name = "dosage_instruction", columnDefinition = "TEXT")
    private String dosageInstruction;

    @Column(name = "priority")
    private String priority;

    @Column(name = "authored_on")
    private String authoredOn;
}

