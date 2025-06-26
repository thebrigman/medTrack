package org.example.fhirplay.mapper;

import ca.uhn.fhir.context.FhirContext;
import org.example.fhirplay.model.*;
import org.example.fhirplay.repo.MedicationRepository;
import org.example.fhirplay.repo.PatientRepository;
import org.example.fhirplay.repo.PractitionerRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FhirResourceToEntityMapper {

    private final FhirContext fhirContext;
    private final MedicationRepository medicationRepository;
    private final PatientRepository patientRepository;
    private final PractitionerRepository practitionerRepository;

    @Autowired
    public FhirResourceToEntityMapper(MedicationRepository medicationRepository,
                                      PatientRepository patientRepository,
                                      PractitionerRepository practitionerRepository) {
        this.fhirContext = FhirContext.forR4();
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
        this.practitionerRepository = practitionerRepository;
    }

    public PatientEntity toPatientEntity(Patient fhirPatient) {
        PatientEntity entity = new PatientEntity();

        // Map ID
        if (fhirPatient.hasId()) {
            entity.setFhirId(fhirPatient.getIdElement().getIdPart());
        }else {
            entity.setFhirId(null);
        }

        return setPatientFields(fhirPatient, entity);
    }

    public PatientEntity setPatientFields(Patient fhirPatient, PatientEntity entity) {
        // Set resource type
        entity.setResourceType(fhirPatient.getResourceType().toString());

        // Map Name (taking first name entry if multiple exist)
        if (fhirPatient.hasName()) {
            HumanName name = fhirPatient.getNameFirstRep();
            if (name.hasFamily()) {
                entity.setFamilyName(name.getFamily());
            }
            if (name.hasGiven()) {
                // Taking first given name if multiple exist
                entity.setGivenName(name.getGivenAsSingleString().split(" ")[0]);
            }
        }

        // Map Gender
        if (fhirPatient.hasGender()) {
            entity.setGender(fhirPatient.getGender().toCode());
        }

        // Map Birth Date
        if (fhirPatient.hasBirthDate()) {
            entity.setBirthDate(fhirPatient.getBirthDateElement().getValueAsString());
        }

        // Map Address (taking first address if multiple exist)
        if (fhirPatient.hasAddress()) {
            Address address = fhirPatient.getAddressFirstRep();
            if (address.hasLine()) {
                // Taking first line if multiple exist
                entity.setAddressLine(address.getLine().get(0).getValue());
            }
            if (address.hasCity()) {
                entity.setCity(address.getCity());
            }
            if (address.hasPostalCode()) {
                entity.setPostalCode(address.getPostalCode());
            }
            if (address.hasCountry()) {
                entity.setCountry(address.getCountry());
            }
        }

        // Map Phone Number (taking first phone if multiple exist)
        if (fhirPatient.hasTelecom()) {
            List<ContactPoint> telecoms = fhirPatient.getTelecom();
            for (ContactPoint contact : telecoms) {
                if (contact.hasSystem() && contact.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                    entity.setPhoneNumber(contact.getValue());
                    break; // Take first phone number
                }
            }
        }
        return entity;
    }

    public PractitionerEntity toPractitionerEntity(Practitioner fhirPractitioner) {
        PractitionerEntity entity = new PractitionerEntity();

        // Map ID
        if (fhirPractitioner.hasId()) {
            entity.setFhirId(fhirPractitioner.getIdElement().getIdPart());
        }

        return mapPractitionerFields(fhirPractitioner, entity);
    }

    public PractitionerEntity mapPractitionerFields(Practitioner fhirPractitioner, PractitionerEntity entity) {
        // Map Name (taking first name entry if multiple exist)
        if (fhirPractitioner.hasName()) {
            HumanName name = fhirPractitioner.getNameFirstRep();
            if (name.hasFamily()) {
                entity.setFamilyName(name.getFamily());
            }
            if (name.hasGiven()) {
                entity.setGivenName(name.getGivenAsSingleString().split(" ")[0]); // Taking first given name
            }
        }

        // Map Gender
        if (fhirPractitioner.hasGender()) {
            entity.setGender(fhirPractitioner.getGender().toCode());
        }

        // Map Qualification
        if (fhirPractitioner.hasQualification() && !fhirPractitioner.getQualification().isEmpty()) {
            entity.setQualification(fhirPractitioner.getQualificationFirstRep().getCode().getText());
        }

        // Map Phone Number (taking first phone if multiple exist)
        if (fhirPractitioner.hasTelecom()) {
            List<ContactPoint> telecoms = fhirPractitioner.getTelecom();
            for (ContactPoint contact : telecoms) {
                if (contact.hasSystem() && contact.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                    entity.setPhoneNumber(contact.getValue());
                    break;
                }
            }
        }

        // Map Email
        if (fhirPractitioner.hasTelecom()) {
            for (ContactPoint contact : fhirPractitioner.getTelecom()) {
                if (contact.hasSystem() && contact.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
                    entity.setEmail(contact.getValue());
                    break;
                }
            }
        }

        return entity;
    }

    public MedicationEntity toMedicationEntity(Medication fhirMedication) {
        MedicationEntity medEntity = new MedicationEntity();

        medEntity.setResourceType(fhirMedication.fhirType()); // "Medication"


        medEntity.setFhirId(fhirMedication.getIdPart());


        return setMedicationFields(fhirMedication, medEntity);
    }

    public MedicationEntity setMedicationFields(Medication fhirMedication, MedicationEntity medEntity) {
        if (fhirMedication.hasForm()) {
            medEntity.setForm(fhirMedication.getForm().getCodingFirstRep().getDisplay());
        }

        medEntity.setMedicationName(fhirMedication.getCode().getCodingFirstRep().getDisplay());

        if (fhirMedication.hasIngredient()) {
            Medication.MedicationIngredientComponent ingredient = fhirMedication.getIngredientFirstRep();
            if (ingredient.hasStrength() && ingredient.getStrength().hasNumerator()) {
                medEntity.setStrengthValue(ingredient.getStrength().getNumerator().getValue().doubleValue());
                medEntity.setStrengthUnit(ingredient.getStrength().getNumerator().getUnit());
            }
        }

        medEntity.setFhirJson(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirMedication));

        return medEntity;
    }

    public MedicationAdministrationEntity toMedicationAdminEntity(MedicationAdministration fhirAdmin) {
        MedicationAdministrationEntity entity = new MedicationAdministrationEntity();
        //Map fhirId
        entity.setFhirId(fhirAdmin.hasId() ? fhirAdmin.getIdPart() : null);

        return setMedicationAdministrationEntityFields(fhirAdmin, entity);
    }

    public MedicationAdministrationEntity setMedicationAdministrationEntityFields(MedicationAdministration fhirAdmin, MedicationAdministrationEntity entity) {
        //Map patient
        if (fhirAdmin.hasSubject() && fhirAdmin.getSubject().hasReference()) {
            String patientFhirId = fhirAdmin.getSubject().getReference().replace("Patient/", ""); // "pat1"
            PatientEntity patient = patientRepository.findByFhirId(patientFhirId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient with FHIR ID '" + patientFhirId + "' not found"));
            entity.setPatient(patient);
        }

        //Map status
        if (fhirAdmin.hasStatus()) {
            entity.setStatus(fhirAdmin.getStatus().toCode()); // "completed"
        }

        // Map Effective DateTime
        if (fhirAdmin.hasEffectiveDateTimeType()) {
            entity.setEffectiveDateTime(fhirAdmin.getEffectiveDateTimeType().getValueAsString()); // "2025-03-10T08:30:00-05:00"
        }

        //Map dosage quantity, unit, and route
        if(fhirAdmin.getDosage().hasDose()) {
            entity.setDosageQuantity(fhirAdmin.getDosage().getDose().getValue().doubleValue());
            entity.setDosageUnit(fhirAdmin.getDosage().getDose().getUnit());
            entity.setDosageRoute(fhirAdmin.getDosage().getRoute().getCodingFirstRep().getDisplay());
        }

        //Map medication
        if (fhirAdmin.hasMedicationReference() && fhirAdmin.getMedicationReference().hasReference()) {
            String medFhirId = fhirAdmin.getMedicationReference().getReference().replace("Medication/", "");
            MedicationEntity medication = medicationRepository.findByFhirId(medFhirId)
                    .orElseThrow(() -> new IllegalArgumentException("Medication with FHIR ID '" + medFhirId + "' not found"));
            entity.setMedication(medication); // Sets null if not found
        }

        //Map fhirJson
        entity.setFhirJson(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirAdmin));
        return entity;
    }

    public MedicationRequestEntity toMedRequestEntity(MedicationRequest fhirRequest) {
        MedicationRequestEntity entity = new MedicationRequestEntity();

        // Map FHIR ID
        if (fhirRequest.hasId()) {
            entity.setFhirId(fhirRequest.getIdElement().getIdPart());
        }

        return setMedicationRequestFields(fhirRequest, entity);
    }

    public MedicationRequestEntity setMedicationRequestFields(MedicationRequest fhirRequest, MedicationRequestEntity entity) {
        // Map Patient
        if (fhirRequest.hasSubject() && fhirRequest.getSubject().hasReference()) {
            String patientFhirId = fhirRequest.getSubject().getReference().replace("Patient/", "");
            PatientEntity patient = patientRepository.findByFhirId(patientFhirId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient with FHIR ID '" + patientFhirId + "' not found"));
            entity.setPatient(patient);
        }

        // Map Practitioner (Prescriber)
        if (fhirRequest.hasRequester() && fhirRequest.getRequester().hasReference()) {
            String practitionerFhirId = fhirRequest.getRequester().getReference().replace("Practitioner/", "");
            PractitionerEntity practitioner = practitionerRepository.findByFhirId(practitionerFhirId)
                    .orElseThrow(() -> new IllegalArgumentException("Practitioner with FHIR ID '" + practitionerFhirId + "' not found"));
            entity.setPractitioner(practitioner);
        }

        // Map Medication
        if (fhirRequest.hasMedicationReference() && fhirRequest.getMedicationReference().hasReference()) {
            String medFhirId = fhirRequest.getMedicationReference().getReference().replace("Medication/", "");
            MedicationEntity medication = medicationRepository.findByFhirId(medFhirId)
                    .orElseThrow(() -> new IllegalArgumentException("Medication with FHIR ID '" + medFhirId + "' not found"));
            entity.setMedication(medication);
        }

        // Map Status
        if (fhirRequest.hasStatus()) {
            entity.setStatus(fhirRequest.getStatus().toCode());
        }

        // Map Intent
        if (fhirRequest.hasIntent()) {
            entity.setIntent(fhirRequest.getIntent().toCode());
        }

        // Map Priority
        if (fhirRequest.hasPriority()) {
            entity.setPriority(fhirRequest.getPriority().toCode());
        }

        // Map AuthoredOn Date
        if (fhirRequest.hasAuthoredOn()) {
            entity.setAuthoredOn(fhirRequest.getAuthoredOnElement().getValueAsString());
        }

        // Map Dosage Instructions
        if (fhirRequest.hasDosageInstruction()) {
            entity.setDosageInstruction(fhirRequest.getDosageInstructionFirstRep().getText());
        }

        // Store Full FHIR JSON
        entity.setFhirJson(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirRequest));

        return entity;
    }

}
