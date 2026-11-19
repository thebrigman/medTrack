# medTrack

A Spring Boot service that tracks medication orders and doses, storing them in PostgreSQL and exposing them as **HL7 FHIR R4** resources. It also has a small web UI for the nurse workflow: see active orders, record a dose, and review what was given.

## Tech stack

- Java 17, Spring Boot 3.4 (Web, Data JPA, Thymeleaf)
- [HAPI FHIR](https://hapifhir.io/) 8.0 for FHIR R4 models and JSON parsing
- PostgreSQL 15
- Lombok, Maven (wrapper included)

## Running locally

**1. Start PostgreSQL** (the `db` service from `docker-compose.yaml`):

```bash
docker compose up -d db
```

This creates database `fhir` with user `admin` / password `password` on `localhost:5432`, the same settings `application.properties` uses.

**2. Start the app**, from your IDE (`MedTrackApplication`) or with:

```bash
./mvnw spring-boot:run
```

**3. Open** <http://localhost:8080/>.

The schema is recreated on every start (`ddl-auto=create-drop`) and `src/main/resources/data.sql` loads sample data. Any data you add is gone after a restart.

### Running everything in Docker

The `Dockerfile` copies a pre-built jar, so build it first:

```bash
./mvnw clean package -DskipTests
docker compose up --build
```

In Docker the app connects to `db:5432` and uses `ddl-auto=update`.

## Sample data

| Patients | Practitioners | Medications | Requests |
|---|---|---|---|
| `pat1` John Doe | `prac1` Dr. Emily Carter | `med1` Ibuprofen | `medreq1` Ibuprofen for John Doe |
| `pat2` Jane Smith | `prac2` Dr. Michael Lee | `med2` Amoxicillin | `medreq2` Amoxicillin for Jane Smith |
| `pat3` Alex Patel | `prac3` Dr. Sarah Patel | `med3` Acetaminophen | `medreq3` Acetaminophen for Alex Patel (completed) |
| `pat4` Maria Gonzalez | `prac4` Dr. James Nguyen | `med4` Albuterol | `medreq4` Albuterol for Maria Gonzalez |
| `pat5` Liam Chen | `prac5` Dr. Olivia Brown | `med5` Lisinopril | `medreq5` Lisinopril for Liam Chen |

## Web UI

| Page | URL |
|---|---|
| Active medication requests | `/ui/requests` (also `/`) |
| New medication request | `/ui/requests/new` |
| Log a dose against a request | `/ui/requests/{requestId}/log` |
| Recent administrations | `/ui/administrations` |
| Administration details | `/ui/administrations/{id}` |

The log-dose form is pre-filled from the order and shows the patient's previous doses of the same medication. The UI builds FHIR resources and saves them through the same services as the REST API, so both paths store data the same way.

## REST API

All endpoints accept and return FHIR R4 JSON. Resource ids are FHIR logical ids such as `pat1`, not database ids.

| Resource | Create | Read one | List | Update | Delete |
|---|---|---|---|---|---|
| Patient | `POST /patients` | `GET /patients/{id}` | `GET /patients` | `PUT /patients` | `DELETE /patients/{id}` |
| Practitioner | `POST /practitioner` | `GET /practitioner/{id}` | `GET /practitioner` | `PUT /practitioner` | `DELETE /practitioner/{id}` |
| Medication | `POST /medication` | `GET /medication/{id}` | `GET /medication` | `PUT /medication` | `DELETE /medication/{id}` |
| MedicationRequest | `POST /medication-request` | `GET /medication-request/{id}` | `GET /medication-requests` | `PUT /medication-request` | `DELETE /medication-request/{id}` |
| MedicationAdministration | `POST /medication-administration` | `GET /medication-administration/{id}` | `GET /medication-administration` | `PUT /medication-administration` | `DELETE /medication-administration/{id}` |

`GET /patient_bundle/{patientId}` returns a FHIR `Bundle` with the patient, their medication requests and administrations, and the medications those administrations use.

Lists are returned as `Bundle`s of type `collection`. `PUT` takes the full resource, and its `id` identifies which record to update.

### Example: record a dose

```bash
curl -X POST http://localhost:8080/medication-administration \
  -H "Content-Type: application/json" \
  -d '{
    "resourceType": "MedicationAdministration",
    "id": "medadmin1",
    "status": "completed",
    "medicationReference": { "reference": "Medication/med1" },
    "subject": { "reference": "Patient/pat1" },
    "performer": [ { "actor": { "reference": "Practitioner/prac1" } } ],
    "effectiveDateTime": "2025-03-10T08:30:00-05:00",
    "dosage": {
      "route": { "coding": [ { "system": "http://snomed.info/sct", "code": "26643006", "display": "Oral" } ] },
      "dose": { "value": 500, "unit": "mg", "system": "http://unitsofmeasure.org", "code": "mg" }
    }
  }'
```

`id` is required and must be unique. The referenced patient, medication and practitioner must already exist.

## Project structure

```
src/main/java/org/example/fhirplay/
├── controller/   REST endpoints (FHIR JSON in and out)
├── service/      business logic; parses FHIR JSON and saves through repositories
├── mapper/       FhirResourceToEntityMapper / EntityToFhirResourceMapper
├── model/        JPA entities, one table per resource
├── repo/         Spring Data JPA repositories
├── ui/           Thymeleaf controller, form objects, template helpers
└── FhirConfig    shared FhirContext (R4) bean

src/main/resources/
├── templates/    Thymeleaf pages (+ fragments/layout.html)
├── static/css/   app.css
└── data.sql      sample data
```

Resources are stored as plain columns, not FHIR JSON, and the two mapper classes convert between them. Fields without a column (for example `note`, or extra performers) are dropped when a resource is saved.

## Known limitations

- **Errors:** most failures, such as an unknown id or a missing reference, return HTTP 500 rather than 404 or 400.
- **Deletes:** deleting a patient also deletes their requests and administrations, and deleting a practitioner also deletes their administrations. Deleting a practitioner or medication that a request still references fails.
- **Authored dates:** `MedicationRequest.authoredOn` must be a plain date (`2025-03-01`). A date with a time saves, but reading that request back fails.
- **Request link:** the link from an administration to its request (`MedicationAdministration.request`) isn't stored.
- **Security:** there's no authentication.
