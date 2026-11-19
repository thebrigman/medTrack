package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.mapper.EntityToFhirResourceMapper;
import org.example.fhirplay.model.MedicationAdministrationEntity;
import org.example.fhirplay.model.MedicationEntity;
import org.example.fhirplay.model.MedicationRequestEntity;
import org.example.fhirplay.model.PatientEntity;
import org.example.fhirplay.repo.MedicationAdministrationRepository;
import org.example.fhirplay.repo.MedicationRepository;
import org.example.fhirplay.repo.MedicationRequestRepository;
import org.example.fhirplay.repo.PatientRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PatientFHIRBundleService {

    private final PatientRepository patientRepository;
    private final MedicationAdministrationRepository medAdminRepository;
    private final MedicationRequestRepository medRequestRepository;
    private final MedicationRepository medicationRepository;
    private final EntityToFhirResourceMapper toFhirResourceMapper;
    private final FhirContext fhirContext;

    @Autowired
    public PatientFHIRBundleService(
            PatientRepository patientRepository,
            MedicationAdministrationRepository medAdminRepository,
            MedicationRequestRepository medRequestRepository,
            MedicationRepository medicationRepository, EntityToFhirResourceMapper toFhirResourceMapper,
            FhirContext fhirContext) {
        this.patientRepository = patientRepository;
        this.medAdminRepository = medAdminRepository;
        this.medRequestRepository = medRequestRepository;
        this.medicationRepository = medicationRepository;
        this.toFhirResourceMapper = toFhirResourceMapper;
        this.fhirContext = fhirContext;
    }

    public String getPatientBundle(String patientFhirId) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Fetch patient
        PatientEntity patientEntity = patientRepository.findByFhirId(patientFhirId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Patient patient = toFhirResourceMapper.toFhirPatient(patientEntity);
        String fhirJson = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        Patient fhirPatient = fhirContext.newJsonParser().parseResource(Patient.class, fhirJson);
        bundle.addEntry().setResource(fhirPatient);

        // Fetch MedicationAdministrations for the patient
        List<MedicationAdministrationEntity> medAdmins = medAdminRepository.findMedicationAdministrationEntitiesByPatient_FhirId(patientFhirId);

        for (MedicationAdministrationEntity medAdminEntity : medAdmins) {
            MedicationAdministration fhirMedAdmin = toFhirResourceMapper.toFhirMedicationAdministration(medAdminEntity);
            bundle.addEntry().setResource(fhirMedAdmin);
        }

        // Fetch MedicationRequests for the patient
        List<MedicationRequestEntity> medRequests = medRequestRepository.findMedicationRequestEntitiesByPatient_FhirId(patientFhirId);
        for (MedicationRequestEntity medReqEntity : medRequests) {
            MedicationRequest medicationRequest = toFhirResourceMapper.toFhirMedicationRequest(medReqEntity);
            String requestJson = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medicationRequest);
            MedicationRequest fhirMedRequest = fhirContext.newJsonParser().parseResource(MedicationRequest.class, requestJson);
            bundle.addEntry().setResource(fhirMedRequest);
        }

        // Fetch Medications referenced in MedicationRequests and Administrations
        Set<String> medicationFhirIds = new HashSet<>();
//        medRequests.forEach(req -> medicationFhirIds.add(req.getMedication().getFhirId()));
        medAdmins.forEach(admin -> medicationFhirIds.add(admin.getMedication().getFhirId()));

        for (String medFhirId : medicationFhirIds) {
            MedicationEntity medicationEntity = medicationRepository.findByFhirId(medFhirId)
                    .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
            Medication fhirMedication = toFhirResourceMapper.toFhirMedication(medicationEntity);
            bundle.addEntry().setResource(fhirMedication);
        }

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }
}

