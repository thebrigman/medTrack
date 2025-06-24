package org.example.fhirplay.mapper;

import org.example.fhirplay.model.PatientEntity;
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
}
