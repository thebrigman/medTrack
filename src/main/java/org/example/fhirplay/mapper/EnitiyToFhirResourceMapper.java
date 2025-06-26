package org.example.fhirplay.mapper;

import org.example.fhirplay.model.PatientEntity;
import org.example.fhirplay.model.PractitionerEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

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
}
