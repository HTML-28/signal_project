package com.alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.util.List;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Unit tests for AlertGenerator.
 * Each test simulates a patient scenario and checks alert logic.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AlertGeneratorTest {

    private AlertGenerator alertGenerator;
    private DataStorage dataStorage;
    private Patient patient;
    private static final int PATIENT_ID = 123;

    @BeforeEach
    void init() {
        dataStorage = DataStorage.getInstance();
        alertGenerator = new AlertGenerator(dataStorage);
        patient = new Patient(PATIENT_ID);
    }

    // --- Helper Methods ---

    /**
     * Adds a record to the patient and evaluates data.
     * @param value the measurement value
     * @param type the type of measurement (e.g., "SystolicBP")
     * @return list of active alerts for the patient
     */
    private List<Alert> addAndEval(double value, String type) {
        patient.addRecord(value, type, System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        return alertGenerator.getActiveAlertsForPatient(PATIENT_ID);
    }

    /**
     * Checks if an alert of the given type exists in the list.
     * @param alerts list of alerts
     * @param type alert type to check
     * @return true if alert of the given type exists
     */
    private boolean containsAlert(List<Alert> alerts, AlertType type) {
        return alerts.stream().anyMatch(a -> a.getType() == type);
    }

    /**
     * Returns the first alert of the given type, or null if not found.
     * @param alerts list of alerts
     * @param type alert type to find
     * @return the alert or null
     */
    private Alert getAlert(List<Alert> alerts, AlertType type) {
        return alerts.stream().filter(a -> a.getType() == type).findFirst().orElse(null);
    }

    // --- Test Cases ---

    /**
     * Test: High systolic BP should trigger a CRITICAL alert.
     */
    @Test
    @DisplayName("High systolic BP triggers CRITICAL alert")
    void highSystolicBP() {
        List<Alert> alerts = addAndEval(185.0, "SystolicBP");
        Alert alert = getAlert(alerts, AlertType.HIGH_SYSTOLIC_BP);
        assertNotNull(alert, "Expected HIGH_SYSTOLIC_BP alert");
        assertEquals(AlertSeverity.CRITICAL, alert.getSeverity());
    }

    /**
     * Test: Low systolic BP should trigger a HIGH alert.
     */
    @Test
    @DisplayName("Low systolic BP triggers HIGH alert")
    void lowSystolicBP() {
        List<Alert> alerts = addAndEval(85.0, "SystolicBP");
        Alert alert = getAlert(alerts, AlertType.LOW_SYSTOLIC_BP);
        assertNotNull(alert, "Expected LOW_SYSTOLIC_BP alert");
        assertEquals(AlertSeverity.HIGH, alert.getSeverity());
    }

    /**
     * Test: High diastolic BP should trigger an alert.
     */
    @Test
    @DisplayName("High diastolic BP triggers alert")
    void highDiastolicBP() {
        List<Alert> alerts = addAndEval(130.0, "DiastolicBP");
        assertTrue(containsAlert(alerts, AlertType.HIGH_DIASTOLIC_BP));
    }

    /**
     * Test: Low diastolic BP should trigger an alert.
     */
    @Test
    @DisplayName("Low diastolic BP triggers alert")
    void lowDiastolicBP() {
        List<Alert> alerts = addAndEval(55.0, "DiastolicBP");
        assertTrue(containsAlert(alerts, AlertType.LOW_DIASTOLIC_BP));
    }

    /**
     * Test: Increasing BP trend should trigger a trend alert.
     */
    @Test
    @DisplayName("Increasing BP trend triggers trend alert")
    void increasingBPTrend() {
        long now = System.currentTimeMillis();
        patient.addRecord(140.0, "SystolicBP", now - 3000);
        patient.addRecord(155.0, "SystolicBP", now - 2000);
        patient.addRecord(170.0, "SystolicBP", now - 1000);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getActiveAlertsForPatient(PATIENT_ID);
        assertTrue(containsAlert(alerts, AlertType.BP_INCREASING_TREND));
    }

    /**
     * Test: Decreasing BP trend should trigger a trend alert.
     */
    @Test
    @DisplayName("Decreasing BP trend triggers trend alert")
    void decreasingBPTrend() {
        long now = System.currentTimeMillis();
        patient.addRecord(170.0, "SystolicBP", now - 3000);
        patient.addRecord(155.0, "SystolicBP", now - 2000);
        patient.addRecord(140.0, "SystolicBP", now - 1000);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getActiveAlertsForPatient(PATIENT_ID);
        assertTrue(containsAlert(alerts, AlertType.BP_DECREASING_TREND));
    }

    /**
     * Test: Normal BP should not trigger any BP alerts.
     */
    @Test
    @DisplayName("Normal BP does not trigger alerts")
    void normalBP() {
        List<Alert> alerts = addAndEval(120.0, "SystolicBP");
        assertFalse(containsAlert(alerts, AlertType.HIGH_SYSTOLIC_BP));
        assertFalse(containsAlert(alerts, AlertType.LOW_SYSTOLIC_BP));
    }

    /**
     * Test: Low oxygen saturation should trigger a HIGH alert.
     */
    @Test
    @DisplayName("Low oxygen saturation triggers HIGH alert")
    void lowOxygen() {
        List<Alert> alerts = addAndEval(90.0, "OxygenSaturation");
        Alert alert = getAlert(alerts, AlertType.LOW_OXYGEN_SATURATION);
        assertNotNull(alert, "Expected LOW_OXYGEN_SATURATION alert");
        assertEquals(AlertSeverity.HIGH, alert.getSeverity());
    }

    /**
     * Test: Rapid oxygen drop should trigger an alert.
     */
    @Test
    @DisplayName("Rapid oxygen drop triggers alert")
    void rapidOxygenDrop() {
        long now = System.currentTimeMillis();
        patient.addRecord(98.0, "OxygenSaturation", now - 500000);
        patient.addRecord(92.0, "OxygenSaturation", now);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getActiveAlertsForPatient(PATIENT_ID);
        assertTrue(containsAlert(alerts, AlertType.RAPID_OXYGEN_DROP));
    }

    /**
     * Test: Combined hypotensive hypoxemia should trigger a CRITICAL alert.
     */
    @Test
    @DisplayName("Combined hypotensive hypoxemia triggers CRITICAL alert")
    void hypotensiveHypoxemia() {
        long now = System.currentTimeMillis();
        patient.addRecord(85.0, "SystolicBP", now);
        patient.addRecord(91.0, "OxygenSaturation", now);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getActiveAlertsForPatient(PATIENT_ID);
        Alert alert = getAlert(alerts, AlertType.HYPOTENSIVE_HYPOXEMIA);
        assertNotNull(alert, "Expected HYPOTENSIVE_HYPOXEMIA alert");
        assertEquals(AlertSeverity.CRITICAL, alert.getSeverity());
    }

    /**
     * Test: Abnormal ECG peak should trigger an alert.
     */
    @Test
    @DisplayName("ECG abnormal peak triggers alert")
    void ecgAbnormalPeak() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 19; i++) {
            patient.addRecord(70.0 + (Math.random() * 2), "ECG", now - (20 - i) * 1000);
        }
        patient.addRecord(120.0, "ECG", now);
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getActiveAlertsForPatient(PATIENT_ID);
        assertTrue(containsAlert(alerts, AlertType.ECG_ABNORMAL_PEAK));
    }

    /**
     * Test: Manual trigger alert should be detected.
     */
    @Test
    @DisplayName("Manual trigger alert is detected")
    void manualTrigger() {
        patient.addRecord(1.0, "Alert", System.currentTimeMillis(), "triggered");
        alertGenerator.evaluateData(patient);
        List<Alert> alerts = alertGenerator.getActiveAlertsForPatient(PATIENT_ID);
        assertTrue(containsAlert(alerts, AlertType.MANUAL_TRIGGER));
    }

    /**
     * Test: Alert should be resolved when condition normalizes.
     */
    @Test
    @DisplayName("Alert is resolved when condition normalizes")
    void alertResolution() {
        // Trigger alert
        patient.addRecord(185.0, "SystolicBP", System.currentTimeMillis() - 1000);
        alertGenerator.evaluateData(patient);
        assertTrue(containsAlert(alertGenerator.getActiveAlertsForPatient(PATIENT_ID), AlertType.HIGH_SYSTOLIC_BP));
        // Add normal reading to resolve
        patient.addRecord(120.0, "SystolicBP", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        assertFalse(containsAlert(alertGenerator.getActiveAlertsForPatient(PATIENT_ID), AlertType.HIGH_SYSTOLIC_BP));
    }

    /**
     * Test: Get all active alerts for multiple patients.
     */
    @Test
    @DisplayName("Get all active alerts for multiple patients")
    void getAllActiveAlerts() {
        Patient patient2 = new Patient(456);
        patient.addRecord(185.0, "SystolicBP", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
        patient2.addRecord(91.0, "OxygenSaturation", System.currentTimeMillis());
        alertGenerator.evaluateData(patient2);
        List<Alert> allAlerts = alertGenerator.getAllActiveAlerts();
        boolean found1 = allAlerts.stream().anyMatch(a -> a.getPatientId() == PATIENT_ID);
        boolean found2 = allAlerts.stream().anyMatch(a -> a.getPatientId() == 456);
        assertTrue(found1, "Alert for patient 1 expected");
        assertTrue(found2, "Alert for patient 2 expected");
    }
}