package org.example.fhirplay.service;

import ca.uhn.fhir.context.FhirContext;
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

    @Autowired
    public PatientFHIRBundleService(
            PatientRepository patientRepository,
            MedicationAdministrationRepository medAdminRepository,
            MedicationRequestRepository medRequestRepository,
            MedicationRepository medicationRepository) {
        this.patientRepository = patientRepository;
        this.medAdminRepository = medAdminRepository;
        this.medRequestRepository = medRequestRepository;
        this.medicationRepository = medicationRepository;
    }

    private static final FhirContext fhirContext = FhirContext.forR4();

    public String getPatientBundle(String patientFhirId) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Fetch patient
        PatientEntity patientEntity = patientRepository.findByFhirId(patientFhirId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Patient fhirPatient = fhirContext.newJsonParser().parseResource(Patient.class, patientEntity.getFhirJson());
        bundle.addEntry().setResource(fhirPatient);

        // Fetch MedicationAdministrations for the patient
        List<MedicationAdministrationEntity> medAdmins = medAdminRepository.findMedicationAdministrationEntitiesByPatient_FhirId(patientFhirId);

        for (MedicationAdministrationEntity medAdminEntity : medAdmins) {
            MedicationAdministration fhirMedAdmin = fhirContext.newJsonParser().parseResource(MedicationAdministration.class, medAdminEntity.getFhirJson());
            bundle.addEntry().setResource(fhirMedAdmin);
        }

        // Fetch MedicationRequests for the patient
        List<MedicationRequestEntity> medRequests = medRequestRepository.findMedicationRequestEntitiesByPatient_FhirId(patientFhirId);
        for (MedicationRequestEntity medReqEntity : medRequests) {
            MedicationRequest fhirMedRequest = fhirContext.newJsonParser().parseResource(MedicationRequest.class, medReqEntity.getFhirJson());
            bundle.addEntry().setResource(fhirMedRequest);
        }

        // Fetch Medications referenced in MedicationRequests and Administrations
        Set<String> medicationFhirIds = new HashSet<>();
//        medRequests.forEach(req -> medicationFhirIds.add(req.getMedication().getFhirId()));
        medAdmins.forEach(admin -> medicationFhirIds.add(admin.getMedication().getFhirId()));

        for (String medFhirId : medicationFhirIds) {
            MedicationEntity medicationEntity = medicationRepository.findByFhirId(medFhirId)
                    .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
            Medication fhirMedication = fhirContext.newJsonParser().parseResource(Medication.class, medicationEntity.getFhirJson());
            bundle.addEntry().setResource(fhirMedication);
        }

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }
}

