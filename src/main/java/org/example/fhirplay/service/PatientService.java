package org.example.fhirplay.service;

import org.example.fhirplay.mapper.EnitiyToFhirResourceMapper;
import org.example.fhirplay.mapper.FhirResourceToEntityMapper;
import org.example.fhirplay.model.PatientEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
public class PatientService {

    private final FhirContext fhirContext;
    private final FhirResourceToEntityMapper toEntityMapper;
    private final PatientRepository patientRepository;
    private final EnitiyToFhirResourceMapper toFhirResourceMapper;

    @Autowired
    public PatientService(FhirResourceToEntityMapper toEntityMapper, PatientRepository patientRepository, EnitiyToFhirResourceMapper toFhirResourceMapper) {
        this.toFhirResourceMapper = toFhirResourceMapper;
        this.fhirContext = FhirContext.forR4();
        this.toEntityMapper = toEntityMapper;
        this.patientRepository = patientRepository;
    }

    public String getAllAsFHIR() {
        List<PatientEntity> patientEntities = patientRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Deserialize JSON and add to Bundle
        for (PatientEntity entity : patientEntities) {
            if (entity.getFhirJson() != null) {
                Patient patient = fhirContext.newJsonParser().parseResource(Patient.class, entity.getFhirJson());
                bundle.addEntry().setResource(patient);
            }
        }

        // Serialize the Bundle to JSON
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

    }

    public String getByFhirId(String fhirId) {
        Optional<PatientEntity> patientEntityOptional = patientRepository.findByFhirId(fhirId);
        PatientEntity patientEntity =  patientEntityOptional.orElseThrow(() -> new NoSuchElementException("No such patient with FhirId of " + fhirId + " found."));
        Patient patient = toFhirResourceMapper.toFhirPatient(patientEntity);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
    }

    public String save(String fhirPatientJson) {
        Patient fhirPatient = fhirContext.newJsonParser().parseResource(Patient.class, fhirPatientJson);

        PatientEntity patient = toEntityMapper.toPatientEntity(fhirPatient);

        patientRepository.save(patient);
        return patient.getFhirJson();
    }

    @Transactional
    public String deleteByFhirId(String fhirId) {
        Optional<PatientEntity> patient = patientRepository.findByFhirId(fhirId);
        patientRepository.deleteByFhirId(fhirId);
        return patient.orElseThrow(() -> new NoSuchElementException("No such patient with FhirId of " + fhirId + " found.")).getFhirJson();
    }

    @Transactional
    public PatientEntity update(String fhirPatientJson) {
        Patient updatedPatient = fhirContext.newJsonParser().parseResource(Patient.class, fhirPatientJson);
        Optional<PatientEntity> existingPatientOpt = patientRepository.findByFhirId(updatedPatient.getIdPart());
        PatientEntity existingPatient = existingPatientOpt.orElseThrow(() -> new NoSuchElementException("No such patient with FhirId of " + updatedPatient.getIdPart() + " found."));

        return patientRepository.save(toEntityMapper.setPatientFields(updatedPatient, existingPatient));
    }
}
