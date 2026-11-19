package org.example.fhirplay.ui;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import org.example.fhirplay.model.MedicationAdministrationEntity;
import org.example.fhirplay.model.MedicationRequestEntity;
import org.example.fhirplay.repo.MedicationAdministrationRepository;
import org.example.fhirplay.repo.MedicationRepository;
import org.example.fhirplay.repo.MedicationRequestRepository;
import org.example.fhirplay.repo.PatientRepository;
import org.example.fhirplay.repo.PractitionerRepository;
import org.example.fhirplay.service.MedicationAdministrationService;
import org.example.fhirplay.service.MedicationRequestService;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Nurse workflow:
 *   /ui/requests                      active MedicationRequests
 *   /ui/requests/{id}/log  (GET/POST) record a MedicationAdministration against a request
 *   /ui/administrations/{id}          what was recorded
 *   /ui/administrations               recent administrations
 */
@Controller
public class GuiController {

    private static final String SNOMED = "http://snomed.info/sct";
    private static final String UCUM = "http://unitsofmeasure.org";

    /** SNOMED CT route code -> display. The mapper stores the display text in medication_administration.dosage_route. */
    static final Map<String, String> ROUTES = new LinkedHashMap<>();
    static {
        ROUTES.put("26643006", "Oral");
        ROUTES.put("47625008", "Intravenous");
        ROUTES.put("78421000", "Intramuscular");
        ROUTES.put("34206005", "Subcutaneous");
        ROUTES.put("6064005", "Topical");
        ROUTES.put("447694001", "Inhalation");
    }

    /** FHIR request-priority codes, lowest to highest urgency */
    static final Map<String, String> PRIORITIES = new LinkedHashMap<>();
    static {
        PRIORITIES.put("routine", "Routine");
        PRIORITIES.put("urgent", "Urgent");
        PRIORITIES.put("asap", "ASAP");
        PRIORITIES.put("stat", "STAT");
    }

    private final MedicationRequestRepository requestRepository;
    private final MedicationAdministrationRepository adminRepository;
    private final PractitionerRepository practitionerRepository;
    private final PatientRepository patientRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationAdministrationService adminService;
    private final MedicationRequestService requestService;

    public GuiController(MedicationRequestRepository requestRepository,
                         MedicationAdministrationRepository adminRepository,
                         PractitionerRepository practitionerRepository,
                         PatientRepository patientRepository,
                         MedicationRepository medicationRepository,
                         MedicationAdministrationService adminService,
                         MedicationRequestService requestService) {
        this.requestRepository = requestRepository;
        this.adminRepository = adminRepository;
        this.practitionerRepository = practitionerRepository;
        this.patientRepository = patientRepository;
        this.medicationRepository = medicationRepository;
        this.adminService = adminService;
        this.requestService = requestService;
    }

    @GetMapping({"/", "/ui"})
    public String home() {
        return "redirect:/ui/requests";
    }

    @GetMapping("/ui/requests")
    public String listRequests(Model model) {
        model.addAttribute("requests", requestRepository.findByStatusOrderByAuthoredOnDesc("active"));
        return "requests";
    }

    @GetMapping("/ui/requests/new")
    public String showRequestForm(Model model) {
        MedRequestForm form = new MedRequestForm();
        form.setPriority("routine");
        form.setAuthoredOn(LocalDate.now());
        populateRequestFormModel(model, form, null);
        return "request-form";
    }

    @PostMapping("/ui/requests/new")
    public String submitRequestForm(@ModelAttribute("form") MedRequestForm form,
                                    BindingResult binding,
                                    Model model) {
        String error = binding.hasErrors()
                ? "Check the " + binding.getFieldErrors().get(0).getField() + " field."
                : validate(form);
        if (error != null) {
            populateRequestFormModel(model, form, error);
            return "request-form";
        }

        MedicationRequest saved;
        try {
            saved = requestService.saveMedRequest(toFhir(form));
        } catch (IllegalArgumentException e) {
            populateRequestFormModel(model, form, e.getMessage());
            return "request-form";
        }

        return "redirect:/ui/requests?created=" + saved.getIdPart();
    }

    @GetMapping("/ui/requests/{requestFhirId}/log")
    public String showLogForm(@PathVariable String requestFhirId, Model model) {
        MedicationRequestEntity request = findRequest(requestFhirId);

        // Pre-fill from the order so the nurse mostly just confirms
        MedAdminForm form = new MedAdminForm();
        if (request.getPractitioner() != null) {
            form.setPractitionerFhirId(request.getPractitioner().getFhirId());
        }
        if (request.getMedication() != null) {
            form.setDosageQuantity(request.getMedication().getStrengthValue());
            form.setDosageUnit(request.getMedication().getStrengthUnit());
        }
        form.setRouteCode("26643006"); // Oral
        form.setEffectiveDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        populateFormModel(model, request, form, null);
        return "med-admin-form";
    }

    @PostMapping("/ui/requests/{requestFhirId}/log")
    public String submitLogForm(@PathVariable String requestFhirId,
                                @ModelAttribute("form") MedAdminForm form,
                                BindingResult binding,
                                Model model) {
        MedicationRequestEntity request = findRequest(requestFhirId);

        // Type conversion failures (e.g. "abc" as the dose) land here instead of a 400 page
        String error = binding.hasErrors()
                ? "Check the " + binding.getFieldErrors().get(0).getField() + " field."
                : validate(form, request);
        if (error != null) {
            populateFormModel(model, request, form, error);
            return "med-admin-form";
        }

        MedicationAdministration saved;
        try {
            saved = adminService.saveMedicationAdmin(toFhir(form, request));
        } catch (IllegalArgumentException e) {
            // Raised by the mapper when a referenced patient/practitioner/medication doesn't exist
            populateFormModel(model, request, form, e.getMessage());
            return "med-admin-form";
        }

        // Post/Redirect/Get: refreshing the confirmation page won't log the dose twice
        return "redirect:/ui/administrations/" + saved.getIdPart() + "?saved";
    }

    @GetMapping("/ui/administrations/{fhirId}")
    public String showAdministration(@PathVariable String fhirId, Model model) {
        MedicationAdministrationEntity admin = adminRepository.findByFhirId(fhirId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No administration " + fhirId));
        model.addAttribute("admin", admin);
        return "med-admin-detail";
    }

    @GetMapping("/ui/administrations")
    public String listAdministrations(Model model) {
        model.addAttribute("admins", adminRepository.findTop50ByOrderByEffectiveDateTimeDesc());
        return "med-admins";
    }

    // ---------------------------------------------------------------------

    private MedicationRequestEntity findRequest(String fhirId) {
        return requestRepository.findByFhirId(fhirId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No medication request " + fhirId));
    }

    private void populateFormModel(Model model, MedicationRequestEntity request, MedAdminForm form, String error) {
        model.addAttribute("request", request);
        model.addAttribute("form", form);
        model.addAttribute("practitioners", practitionerRepository.findAll());
        model.addAttribute("routes", ROUTES);
        model.addAttribute("error", error);

        List<MedicationAdministrationEntity> history = List.of();
        if (request.getPatient() != null && request.getMedication() != null) {
            history = adminRepository.findByPatient_FhirIdAndMedication_FhirIdOrderByEffectiveDateTimeDesc(
                    request.getPatient().getFhirId(), request.getMedication().getFhirId());
        }
        model.addAttribute("history", history);
    }

    private void populateRequestFormModel(Model model, MedRequestForm form, String error) {
        model.addAttribute("form", form);
        model.addAttribute("patients", patientRepository.findAll(Sort.by("familyName", "givenName")));
        model.addAttribute("medications", medicationRepository.findAll(Sort.by("medicationName")));
        model.addAttribute("practitioners", practitionerRepository.findAll(Sort.by("familyName", "givenName")));
        model.addAttribute("priorities", PRIORITIES);
        model.addAttribute("error", error);
    }

    private String validate(MedRequestForm form) {
        if (isBlank(form.getPatientFhirId())) return "Choose a patient.";
        if (isBlank(form.getMedicationFhirId())) return "Choose a medication.";
        if (isBlank(form.getPractitionerFhirId())) return "Choose the prescriber.";
        if (!PRIORITIES.containsKey(form.getPriority())) return "Choose a priority.";
        if (isBlank(form.getDosageInstruction())) return "Enter dosage instructions.";
        if (form.getAuthoredOn() == null) return "Enter the order date.";
        if (form.getAuthoredOn().isAfter(LocalDate.now())) return "Order date can't be in the future.";
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Builds the same FHIR resource a REST client would POST, so it goes through the same mapper. */
    private MedicationRequest toFhir(MedRequestForm form) {
        MedicationRequest request = new MedicationRequest();
        request.setId(UUID.randomUUID().toString());
        request.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        request.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
        request.setPriority(MedicationRequest.MedicationRequestPriority.fromCode(form.getPriority()));
        request.setSubject(new Reference("Patient/" + form.getPatientFhirId()));
        request.setMedication(new Reference("Medication/" + form.getMedicationFhirId()));
        request.setRequester(new Reference("Practitioner/" + form.getPractitionerFhirId()));
        // Day precision: stored as "2025-03-01", which EntityToFhirResourceMapper parses with ISO_DATE.
        // A java.util.Date here would add a time and break that parse.
        request.setAuthoredOnElement(new DateTimeType(form.getAuthoredOn().toString()));
        request.addDosageInstruction().setText(form.getDosageInstruction().trim());
        return request;
    }

    private String validate(MedAdminForm form, MedicationRequestEntity request) {
        if (request.getPatient() == null || request.getMedication() == null) {
            return "This request has no patient or medication, so it can't be administered.";
        }
        if (form.getPractitionerFhirId() == null || form.getPractitionerFhirId().isBlank()) {
            return "Choose who gave the dose.";
        }
        if (form.getDosageQuantity() == null || form.getDosageQuantity() <= 0) {
            return "Dose must be greater than 0.";
        }
        if (form.getDosageUnit() == null || form.getDosageUnit().isBlank()) {
            return "Enter a dose unit.";
        }
        if (!ROUTES.containsKey(form.getRouteCode())) {
            return "Choose a route.";
        }
        if (form.getEffectiveDateTime() == null) {
            return "Enter when the dose was given.";
        }
        if (form.getEffectiveDateTime().isAfter(LocalDateTime.now().plusMinutes(5))) {
            return "Time given can't be in the future.";
        }
        return null;
    }

    /** Builds the same FHIR resource a REST client would POST, so it goes through the same mapper. */
    private MedicationAdministration toFhir(MedAdminForm form, MedicationRequestEntity request) {
        MedicationAdministration admin = new MedicationAdministration();
        admin.setId(UUID.randomUUID().toString());
        admin.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        admin.setSubject(new Reference("Patient/" + request.getPatient().getFhirId()));
        admin.setMedication(new Reference("Medication/" + request.getMedication().getFhirId()));
        admin.addPerformer().setActor(new Reference("Practitioner/" + form.getPractitionerFhirId()));
        admin.setRequest(new Reference("MedicationRequest/" + request.getFhirId()));

        Date given = Date.from(form.getEffectiveDateTime().atZone(ZoneId.systemDefault()).toInstant());
        admin.setEffective(new DateTimeType(given, TemporalPrecisionEnum.SECOND, TimeZone.getDefault()));

        String unit = form.getDosageUnit().trim();
        admin.getDosage()
                .setDose(new Quantity().setValue(form.getDosageQuantity()).setUnit(unit).setSystem(UCUM).setCode(unit))
                .setRoute(new CodeableConcept().addCoding(
                        new Coding(SNOMED, form.getRouteCode(), ROUTES.get(form.getRouteCode()))));
        return admin;
    }
}
