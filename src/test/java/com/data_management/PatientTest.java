package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Patient class, ensuring correct record management.
 */
public class PatientTest {

    private Patient testPatient;
    private static final int TEST_PATIENT_ID = 123;
    private static final long REFERENCE_TIME = System.currentTimeMillis();

    @BeforeEach
    public void initializePatient() {
        // Instantiate a new Patient before each test to ensure isolation
        testPatient = new Patient(TEST_PATIENT_ID);
    }

    @Test
    @DisplayName("Patient object is initialized correctly")
    public void patientInitializationTest() {
        // Check patient ID and that records list is empty
        assertEquals(TEST_PATIENT_ID, testPatient.getPatientId(), "Patient ID should be set correctly");
        assertTrue(testPatient.getAllRecords().isEmpty(), "Patient should start with no records");
    }

    @Test
    @DisplayName("Adding a record without extra info")
    public void addSimpleRecordTest() {
        double tempValue = 36.7;
        String measurementType = "Temperature";
        long timeRecorded = REFERENCE_TIME;

        testPatient.addRecord(tempValue, measurementType, timeRecorded);

        List<PatientRecord> storedRecords = testPatient.getAllRecords();
        assertEquals(1, storedRecords.size(), "Exactly one record should be present");

        PatientRecord rec = storedRecords.get(0);
        assertEquals(TEST_PATIENT_ID, rec.getPatientId(), "Patient ID should match");
        assertEquals(tempValue, rec.getMeasurementValue(), "Measurement value should match");
        assertEquals(measurementType, rec.getRecordType(), "Record type should match");
        assertEquals(timeRecorded, rec.getTimestamp(), "Timestamp should match");
        assertEquals("", rec.getAdditionalInfo(), "Additional info should be empty by default");
    }

    @Test
    @DisplayName("Adding a record with additional information")
    public void addRecordWithInfoTest() {
        double bpValue = 122.5;
        String bpType = "BloodPressure";
        long bpTime = REFERENCE_TIME;
        String extraInfo = "After exercise";

        testPatient.addRecord(bpValue, bpType, bpTime, extraInfo);

        List<PatientRecord> recs = testPatient.getAllRecords();
        assertEquals(1, recs.size(), "Should have one record with additional info");
        assertEquals(extraInfo, recs.get(0).getAdditionalInfo(), "Additional info should be stored correctly");
    }

    @Test
    @DisplayName("Retrieve records within a time interval")
    public void retrieveRecordsByTimeRangeTest() {
        // Add three records at different times
        testPatient.addRecord(36.5, "Temperature", REFERENCE_TIME - 3000);
        testPatient.addRecord(37.0, "Temperature", REFERENCE_TIME - 1500);
        testPatient.addRecord(37.2, "Temperature", REFERENCE_TIME);

        // Retrieve all records in a wide range
        List<PatientRecord> all = testPatient.getRecords(REFERENCE_TIME - 4000, REFERENCE_TIME + 1000);
        assertEquals(3, all.size(), "All records should be retrieved in the full range");

        // Retrieve only the middle record
        List<PatientRecord> middle = testPatient.getRecords(REFERENCE_TIME - 2000, REFERENCE_TIME - 1000);
        assertEquals(1, middle.size(), "Only one record should be in the middle range");
        assertEquals(37.0, middle.get(0).getMeasurementValue(), "Middle record value should match");

        // Retrieve records in a future range (should be empty)
        List<PatientRecord> future = testPatient.getRecords(REFERENCE_TIME + 2000, REFERENCE_TIME + 3000);
        assertTrue(future.isEmpty(), "No records should be found in the future range");
    }

    @Test
    @DisplayName("Retrieve records by exact timestamp")
    public void retrieveByExactTimestampTest() {
        testPatient.addRecord(36.9, "Temperature", REFERENCE_TIME);

        List<PatientRecord> exact = testPatient.getRecords(REFERENCE_TIME, REFERENCE_TIME);
        assertEquals(1, exact.size(), "Should retrieve the record with the exact timestamp");
    }

    @Test
    @DisplayName("Handle invalid time range gracefully")
    public void invalidTimeRangeTest() {
        testPatient.addRecord(36.8, "Temperature", REFERENCE_TIME);

        // End time before start time should yield no results
        List<PatientRecord> invalid = testPatient.getRecords(REFERENCE_TIME + 1000, REFERENCE_TIME - 1000);
        assertTrue(invalid.isEmpty(), "No records should be returned for an invalid time range");
    }

    @Test
    @DisplayName("Group records by their type")
    public void groupRecordsByTypeTest() {
        // Add records of different types
        testPatient.addRecord(36.6, "Temperature", REFERENCE_TIME - 2000);
        testPatient.addRecord(121.0, "BloodPressure", REFERENCE_TIME - 1000);
        testPatient.addRecord(37.1, "Temperature", REFERENCE_TIME);

        Map<String, List<PatientRecord>> grouped = testPatient.getRecordsByType();

        assertEquals(2, grouped.size(), "There should be two types of records");

        List<PatientRecord> temps = grouped.get("Temperature");
        assertEquals(2, temps.size(), "Temperature records count should be correct");

        List<PatientRecord> bps = grouped.get("BloodPressure");
        assertEquals(1, bps.size(), "BloodPressure records count should be correct");
    }

    @Test
    @DisplayName("Retrieve all records and ensure immutability")
    public void getAllRecordsTest() {
        testPatient.addRecord(36.5, "Temperature", REFERENCE_TIME - 2000);
        testPatient.addRecord(120.0, "BloodPressure", REFERENCE_TIME - 1000);
        testPatient.addRecord(37.0, "Temperature", REFERENCE_TIME);

        List<PatientRecord> all = testPatient.getAllRecords();
        assertEquals(3, all.size(), "All records should be returned");

        // Modifying the returned list should not affect the patient's records
        all.clear();
        assertEquals(3, testPatient.getAllRecords().size(), "Original records should remain unchanged");
    }

    @Test
    @DisplayName("Edge case: handle a large number of records")
    public void manyRecordsEdgeCaseTest() {
        // Add 1000 records with incremental values and timestamps
        for (int idx = 0; idx < 1000; idx++) {
            testPatient.addRecord(36.0 + idx * 0.01, "Temperature", REFERENCE_TIME + idx);
        }

        assertEquals(1000, testPatient.getAllRecords().size(), "All 1000 records should be present");

        // Retrieve a subset in a specific time window
        List<PatientRecord> subset = testPatient.getRecords(REFERENCE_TIME + 400, REFERENCE_TIME + 600);
        assertEquals(201, subset.size(), "Subset size should match the expected count");
    }
}