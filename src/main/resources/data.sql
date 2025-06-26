

INSERT INTO patient (
    fhir_id,
    resource_type,
    given_name,
    family_name,
    gender,
    birth_date,
    address_line,
    city,
    postal_code,
    country,
    phone_number
) VALUES
-- Patient 1: John Doe
(
    'pat1',
    'Patient',
    'John',
    'Doe',
    'male',
    '1980-05-15',
    '123 Main St',
    'Boston',
    '02108',
    'USA',
    '555-123-4567'
),

-- Patient 2: Jane Smith
(
    'pat2',
    'Patient',
    'Jane',
    'Smith',
    'female',
    '1992-11-22',
    '456 Elm St',
    'Seattle',
    '98101',
    'USA',
    '555-987-6543'
),

-- Patient 3: Alex Patel
(
    'pat3',
    'Patient',
    'Alex',
    'Patel',
    'other',
    '1975-03-10',
    '789 Oak Ave',
    'Toronto',
    'M5V 2T6',
    'Canada',
    '416-555-7890'
),

-- Patient 4: Maria Gonzalez
(
    'pat4',
    'Patient',
    'Maria',
    'Gonzalez',
    'female',
    '1968-08-30',
    '321 Pine Rd',
    'Miami',
    '33101',
    'USA',
    '305-555-3210'
),

-- Patient 5: Liam Chen
(
    'pat5',
    'Patient',
    'Liam',
    'Chen',
    'male',
    '2000-01-12',
    '654 Birch Ln',
    'Vancouver',
    'V6B 3M9',
    'Canada',
    '604-555-6543'
);

INSERT INTO practitioner (
    fhir_id,
    given_name,
    family_name,
    gender,
    qualification,
    phone_number,
    email
) VALUES
-- Practitioner 1: Dr. Emily Carter (could prescribe Ibuprofen for John Doe)
(
    'prac1',
    'Emily',
    'Carter',
    'female',
    'MD - General Practice',
    '555-0101',
    'emily.carter@clinic.org'
),

-- Practitioner 2: Dr. Michael Lee (could prescribe Amoxicillin for Jane Smith)
(
    'prac2',
    'Michael',
    'Lee',
    'male',
    'MD - Infectious Diseases',
    '555-0102',
    'michael.lee@clinic.org'
),

-- Practitioner 3: Dr. Sarah Patel (could prescribe Acetaminophen for Alex Patel)
(
    'prac3',
    'Sarah',
    'Patel',
    'female',
    'MD - Family Medicine',
    '555-0103',
    'sarah.patel@clinic.org'
),

-- Practitioner 4: Dr. James Nguyen (could prescribe Albuterol for Maria Gonzalez)
(
    'prac4',
    'James',
    'Nguyen',
    'male',
    'MD - Pulmonology',
    '555-0104',
    'james.nguyen@clinic.org'
),

-- Practitioner 5: Dr. Olivia Brown (could prescribe Lisinopril for Liam Chen)
(
    'prac5',
    'Olivia',
    'Brown',
    'female',
    'MD - Cardiology',
    '555-0105',
    'olivia.brown@clinic.org'
);


INSERT INTO medication (
    resource_type,
    medication_name,
    form,
    strength_value,
    strength_unit,
    fhir_id,
    fhir_json
) VALUES
-- Medication 1: Ibuprofen
(
    'Medication',
    'Ibuprofen',
    'tablet',
    200.0,
    'mg',
    'med1',
    '{"resourceType": "Medication", "id": "med1", "code": {"text": "Ibuprofen"}, "form": {"text": "tablet"}, "ingredient": [{"item": {"strength": {"value": 200, "unit": "mg"}}}]}'
),

-- Medication 2: Amoxicillin
(
    'Medication',
    'Amoxicillin',
    'capsule',
    500.0,
    'mg',
    'med2',
    '{"resourceType": "Medication", "id": "med2", "code": {"text": "Amoxicillin"}, "form": {"text": "capsule"}, "ingredient": [{"item": {"strength": {"value": 500, "unit": "mg"}}}]}'
),

-- Medication 3: Acetaminophen
(
    'Medication',
    'Acetaminophen',
    'tablet',
    325.0,
    'mg',
    'med3',
    '{"resourceType": "Medication", "id": "med3", "code": {"text": "Acetaminophen"}, "form": {"text": "tablet"}, "ingredient": [{"item": {"strength": {"value": 325, "unit": "mg"}}}]}'
),

-- Medication 4: Albuterol
(
    'Medication',
    'Albuterol',
    'inhaler',
    90.0,
    'mcg',
    'med4',
    '{"resourceType": "Medication", "id": "med4", "code": {"text": "Albuterol"}, "form": {"text": "inhaler"}, "ingredient": [{"item": {"strength": {"value": 90, "unit": "mcg"}}}]}'
),

-- Medication 5: Lisinopril
(
    'Medication',
    'Lisinopril',
    'tablet',
    10.0,
    'mg',
    'med5',
    '{"resourceType": "Medication", "id": "med5", "code": {"text": "Lisinopril"}, "form": {"text": "tablet"}, "ingredient": [{"item": {"strength": {"value": 10, "unit": "mg"}}}]}'
);

INSERT INTO medication_request (
    patient_id,
    practitioner_id,
    medication_id,
    fhir_id,
    status,
    intent,
    dosage_instruction,
    priority,
    authored_on,
    fhir_json
) VALUES
-- Request 1: Ibuprofen for John Doe
(
    (SELECT id FROM patient WHERE fhir_id = 'pat1'),  -- John Doe
    1,  -- Assuming practitioner with id=1 exists
    (SELECT id FROM medication WHERE fhir_id = 'med1'),  -- Ibuprofen
    'medreq1',
    'active',
    'order',
    'Take 1 tablet every 6 hours as needed for pain',
    'routine',
    '2025-03-01',
    '{"resourceType": "MedicationRequest", "id": "medreq1", "status": "active", "intent": "order", "medicationReference": {"reference": "Medication/med1"}, "subject": {"reference": "Patient/pat1"}, "authoredOn": "2025-03-01", "dosageInstruction": [{"text": "Take 1 tablet every 6 hours as needed for pain"}]}'
),

-- Request 2: Amoxicillin for Jane Smith
(
    (SELECT id FROM patient WHERE fhir_id = 'pat2'),  -- Jane Smith
    1,
    (SELECT id FROM medication WHERE fhir_id = 'med2'),  -- Amoxicillin
    'medreq2',
    'active',
    'order',
    'Take 1 capsule three times daily for 7 days',
    'urgent',
    '2025-03-02',
    '{"resourceType": "MedicationRequest", "id": "medreq2", "status": "active", "intent": "order", "medicationReference": {"reference": "Medication/med2"}, "subject": {"reference": "Patient/pat2"}, "authoredOn": "2025-03-02", "dosageInstruction": [{"text": "Take 1 capsule three times daily for 7 days"}]}'
),

-- Request 3: Acetaminophen for Alex Patel
(
    (SELECT id FROM patient WHERE fhir_id = 'pat3'),  -- Alex Patel
    1,
    (SELECT id FROM medication WHERE fhir_id = 'med3'),  -- Acetaminophen
    'medreq3',
    'completed',
    'order',
    'Take 2 tablets every 4-6 hours as needed for fever',
    'routine',
    '2025-03-03',
    '{"resourceType": "MedicationRequest", "id": "medreq3", "status": "completed", "intent": "order", "medicationReference": {"reference": "Medication/med3"}, "subject": {"reference": "Patient/pat3"}, "authoredOn": "2025-03-03", "dosageInstruction": [{"text": "Take 2 tablets every 4-6 hours as needed for fever"}]}'
),

-- Request 4: Albuterol for Maria Gonzalez
(
    (SELECT id FROM patient WHERE fhir_id = 'pat4'),  -- Maria Gonzalez
    1,
    (SELECT id FROM medication WHERE fhir_id = 'med4'),  -- Albuterol
    'medreq4',
    'active',
    'order',
    'Inhale 2 puffs every 4-6 hours as needed for wheezing',
    'stat',
    '2025-03-04',
    '{"resourceType": "MedicationRequest", "id": "medreq4", "status": "active", "intent": "order", "medicationReference": {"reference": "Medication/med4"}, "subject": {"reference": "Patient/pat4"}, "authoredOn": "2025-03-04", "dosageInstruction": [{"text": "Inhale 2 puffs every 4-6 hours as needed for wheezing"}]}'
),

-- Request 5: Lisinopril for Liam Chen
(
    (SELECT id FROM patient WHERE fhir_id = 'pat5'),  -- Liam Chen
    1,
    (SELECT id FROM medication WHERE fhir_id = 'med5'),  -- Lisinopril
    'medreq5',
    'active',
    'order',
    'Take 1 tablet daily in the morning',
    'routine',
    '2025-03-05',
    '{"resourceType": "MedicationRequest", "id": "medreq5", "status": "active", "intent": "order", "medicationReference": {"reference": "Medication/med5"}, "subject": {"reference": "Patient/pat5"}, "authoredOn": "2025-03-05", "dosageInstruction": [{"text": "Take 1 tablet daily in the morning"}]}'
);