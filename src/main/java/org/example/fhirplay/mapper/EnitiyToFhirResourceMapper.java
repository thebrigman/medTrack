package org.example.fhirplay.mapper;

import org.example.fhirplay.model.MedicationEntity;
import org.example.fhirplay.model.MedicationRequestEntity;
import org.example.fhirplay.model.PatientEntity;
import org.example.fhirplay.model.PractitionerEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class EnitiyToFhirResourceMapper {

    public EnitiyToFhirResourceMapper() {
    }

    public Patient toFhirPatient(PatientEntity entity) {
        Patient patient = new Patient();

        patient.setId(entity.getFhirId());

        patient.setGender(entity.getGender() != null
                ? Enumerations.AdministrativeGender.fromCode(entity.getGender().toLowerCase())
                : null);

        if (entity.getBirthDate() != null)
            patient.setBirthDate(java.sql.Date.valueOf(entity.getBirthDate()));

        // Name
        HumanName name = new HumanName();
        name.setFamily(entity.getFamilyName());
        name.addGiven(entity.getGivenName());
        patient.addName(name);

        // Address
        Address address = new Address();
        address.addLine(entity.getAddressLine());
        address.setCity(entity.getCity());
        address.setPostalCode(entity.getPostalCode());
        address.setCountry(entity.getCountry());
        patient.addAddress(address);

        // Phone
        if (entity.getPhoneNumber() != null) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(entity.getPhoneNumber());
            patient.addTelecom(phone);
        }

        return patient;
    }

    public Practitioner toFhirPractitioner(PractitionerEntity e) {
        Practitioner p = new Practitioner();

        // Logical ID
        p.setId(e.getFhirId());

        // ---------------- Name ----------------
        HumanName name = new HumanName()
                .addGiven(e.getGivenName())
                .setFamily(e.getFamilyName());
        p.addName(name);

        // ---------------- Gender --------------
        if (e.getGender() != null) {
            p.setGender(
                    Enumerations.AdministrativeGender.fromCode(
                            e.getGender().toLowerCase()));
        }

        // ------------- Qualification ----------
        if (e.getQualification() != null) {
            CodeableConcept code = new CodeableConcept()
                    .addCoding(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/v2-0360|2.7") // example
                            .setCode(e.getQualification())
                            .setDisplay(e.getQualification()));
            Practitioner.PractitionerQualificationComponent qual = new Practitioner.PractitionerQualificationComponent()
                    .setCode(code);
            p.addQualification(qual);
        }

        // -------------- Telecom ---------------
        if (e.getPhoneNumber() != null) {
            p.addTelecom(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(e.getPhoneNumber()));
        }
        if (e.getEmail() != null) {
            p.addTelecom(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(e.getEmail()));
        }

        return p;
    }

    public MedicationRequest toFhirMedicationRequest(MedicationRequestEntity e) {

        MedicationRequest r = new MedicationRequest();

        // Logical ID
        r.setId(e.getFhirId());

        // Subject (Patient)
        r.setSubject(toReference(e.getPatient(), "Patient"));

        // Requester (Practitioner)
        r.setRequester(toReference(e.getPractitioner(), "Practitioner"));

        // Medication  (by Reference)
        r.setMedication(toReference(e.getMedication(), "Medication"));

        // Status
        if (e.getStatus() != null) {
            r.setStatus(MedicationRequest.MedicationRequestStatus.fromCode(e.getStatus().toLowerCase()));
        }

        // Intent
        if (e.getIntent() != null) {
            r.setIntent(MedicationRequest.MedicationRequestIntent.fromCode(e.getIntent().toLowerCase()));
        }

        // Priority
        if (e.getPriority() != null) {
            r.setPriority(MedicationRequest.MedicationRequestPriority.fromCode(e.getPriority().toLowerCase()));
        }

        if (e.getAuthoredOn() != null) {
            LocalDate date = LocalDate.parse(e.getAuthoredOn(), DateTimeFormatter.ISO_DATE);
            Date authoredDate = java.sql.Date.valueOf(date); // Convert LocalDate → java.util.Date
            r.setAuthoredOn(authoredDate); // This works if r.setAuthoredOn() wants a java.util.Date
        }

        // DosageInstruction (store free-text in an Annotation)
        if (e.getDosageInstruction() != null) {
            Dosage dosage = new Dosage();
            dosage.setText(e.getDosageInstruction());
            r.addDosageInstruction(dosage);
        }

        return r;
    }
    private Reference toReference(Object entity, String resourceType) {
        if (entity == null) return null;

        String fhirId = switch (resourceType) {
            case "Patient"      -> ((PatientEntity)      entity).getFhirId();
            case "Practitioner" -> ((PractitionerEntity) entity).getFhirId();
            case "Medication"   -> ((MedicationEntity)   entity).getFhirId();
            default -> throw new IllegalArgumentException("Unknown type " + resourceType);
        };

        return new Reference(resourceType + "/" + fhirId);
    }
}
