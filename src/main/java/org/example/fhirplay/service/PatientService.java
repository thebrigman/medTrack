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

    public Bundle getAllAsFHIR() {
        List<PatientEntity> patientEntities = patientRepository.findAll();

        // Create a FHIR Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        // Deserialize JSON and add to Bundle
        for (PatientEntity entity : patientEntities) {
            Patient patient = toFhirResourceMapper.toFhirPatient(entity);
            bundle.addEntry().setResource(patient);
        }
        return bundle;
    }

    public Patient getByFhirId(String fhirId) {
        Optional<PatientEntity> patientEntityOptional = patientRepository.findByFhirId(fhirId);
        PatientEntity patientEntity =  patientEntityOptional.orElseThrow(() -> new NoSuchElementException("No such patient with FhirId of " + fhirId + " found."));
        return toFhirResourceMapper.toFhirPatient(patientEntity);
    }

    public Patient save(String fhirPatientJson) {
        Patient fhirPatient = fhirContext.newJsonParser().parseResource(Patient.class, fhirPatientJson);
        PatientEntity patient = toEntityMapper.toPatientEntity(fhirPatient);
        patientRepository.save(patient);
        return toFhirResourceMapper.toFhirPatient(patient);
    }

    @Transactional
    public Patient deleteByFhirId(String fhirId) {
        Optional<PatientEntity> patientOptional = patientRepository.findByFhirId(fhirId);
        patientRepository.deleteByFhirId(fhirId);
        PatientEntity patientEntity = patientOptional.orElseThrow(() -> new NoSuchElementException("No such patient with FhirId of " + fhirId + " found."));
        return toFhirResourceMapper.toFhirPatient(patientEntity);
    }

    @Transactional
    public Patient update(String fhirPatientJson) {
        Patient updatedPatient = fhirContext.newJsonParser().parseResource(Patient.class, fhirPatientJson);

        //Find patient by id
        Optional<PatientEntity> existingPatientOpt = patientRepository.findByFhirId(updatedPatient.getIdPart());
        PatientEntity existingPatient = existingPatientOpt.orElseThrow(() -> new NoSuchElementException("No such patient with FhirId of " + updatedPatient.getIdPart() + " found."));

        //Update and save patient
        PatientEntity patientEntity = toEntityMapper.setPatientFields(updatedPatient, existingPatient);
        patientRepository.save(patientEntity);

        //Convert to fhir patient resource
        return toFhirResourceMapper.toFhirPatient(patientEntity);
    }
}
