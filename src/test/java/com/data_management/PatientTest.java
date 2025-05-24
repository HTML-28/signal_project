package com.data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for the Patient class, validating record management and retrieval.
 */
public class PatientTest {

    @Test
    public void testPatientIdAndRecordAddition() {
        // Instantiate a Patient with a specific ID
        Patient patient = new Patient(100);

        // Verify patient ID is set correctly
        assertEquals(100, patient.getPatientId(), "Patient ID should match the constructor argument");

        // Add two different types of records
        patient.addRecord(36.5, "Temperature", 1111);
        patient.addRecord(80.0, "HeartRate", 2222);

        // Ensure both records are stored
        List<PatientRecord> allRecords = patient.getAllRecords();
        assertEquals(2, allRecords.size(), "Patient should have two records after addition");
    }

    @Test
    public void testTimeRangeRecordRetrieval() {
        // Create a patient and add records at different timestamps
        Patient p = new Patient(200);
        p.addRecord(37.0, "Temperature", 1000);
        p.addRecord(37.2, "Temperature", 2000);
        p.addRecord(37.4, "Temperature", 3000);

        // Retrieve all records within a broad time range
        List<PatientRecord> rangeRecords = p.getRecords(1000, 3000);
        assertEquals(3, rangeRecords.size(), "Should retrieve all records in the full range");

        // Retrieve records within a narrower time window
        List<PatientRecord> narrowRange = p.getRecords(1500, 2500);
        assertEquals(1, narrowRange.size(), "Should retrieve only one record in the narrow range");
    }

    @Test
    public void testRecordWithAdditionalInfo() {
        // Add a record with extra information
        Patient patient = new Patient(300);
        patient.addRecord(110.0, "BloodPressure", 5000, "After exercise");

        // Fetch all records and check the additional info field
        List<PatientRecord> records = patient.getAllRecords();
        assertEquals("After exercise", records.get(0).getAdditionalInfo(), "Additional info should be stored correctly");
    }

    @Test
    public void testGroupingRecordsByType() {
        // Create a patient and add records of different types
        Patient patient = new Patient(400);
        patient.addRecord(36.8, "Temperature", 100);
        patient.addRecord(120.0, "BloodPressure", 200);
        patient.addRecord(37.1, "Temperature", 300);

        // Group records by their type
        Map<String, List<PatientRecord>> grouped = patient.getRecordsByType();

        // Validate the grouping counts
        assertEquals(2, grouped.get("Temperature").size(), "Temperature group should have two records");
        assertEquals(1, grouped.get("BloodPressure").size(), "BloodPressure group should have one record");
    }
}