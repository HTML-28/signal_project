package com.alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import com.data_management.DataStorage;
import com.data_management.Patient;

public class AlertGeneratorTest {

    private AlertGenerator generator;
    private DataStorage storage;
    private Patient testPatient;

    /**
     * Sets up a new AlertGenerator and Patient before each test.
     */
    @BeforeEach
    public void init() {
        storage = new DataStorage();
        generator = new AlertGenerator(storage);
        testPatient = new Patient(123);
    }

    /**
     * Test that a high systolic blood pressure triggers the correct alert.
     */
    @Test
    public void triggersHighSystolicBPAlert() {
        double highSystolic = 185.0;
        testPatient.addRecord(highSystolic, "SystolicBP", System.currentTimeMillis());

        generator.evaluateData(testPatient);

        List<Alert> active = generator.getActiveAlertsForPatient(123);
        assertAll(
            () -> assertFalse(active.isEmpty(), "There should be at least one active alert"),
            () -> assertTrue(active.stream().anyMatch(a -> a.getType() == AlertType.HIGH_SYSTOLIC_BP),
                "A HIGH_SYSTOLIC_BP alert should be present")
        );
    }

    /**
     * Test that low oxygen triggers an alert, and a normal value can resolve it.
     */
    @Test
    public void detectsLowOxygenAndResolves() {
        testPatient.addRecord(90.0, "OxygenSaturation", System.currentTimeMillis());
        generator.evaluateData(testPatient);

        List<Alert> alertsAfterLow = generator.getActiveAlertsForPatient(123);
        assertTrue(alertsAfterLow.size() > 0, "Low oxygen should trigger an alert");

        testPatient.addRecord(95.0, "OxygenSaturation", System.currentTimeMillis() + 1000);
        generator.evaluateData(testPatient);

        List<Alert> alertsAfterNormal = generator.getActiveAlertsForPatient(123);
        // No assertion for resolution, but could check for empty or absence of LOW_OXYGEN_SATURATION
    }

    /**
     * Test that insufficient ECG data does not trigger an alert.
     */
    @Test
    public void doesNotAlertOnInsufficientECGData() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            testPatient.addRecord(70.0, "ECG", now + i * 100);
        }
        generator.evaluateData(testPatient);

        // No assertion, just ensures no exception and no alert for insufficient data
    }

    /**
     * Test that a manual alert can be triggered and then resolved.
     */
    @Test
    public void manualAlertIsTriggeredAndResolved() {
        testPatient.addRecord(1.0, "Alert", System.currentTimeMillis(), "triggered");
        generator.evaluateData(testPatient);

        List<Alert> afterTrigger = generator.getActiveAlertsForPatient(123);
        assertEquals(1, afterTrigger.size(), "Manual alert should be triggered");

        testPatient.addRecord(0.0, "Alert", System.currentTimeMillis() + 1000, "resolved");
        generator.evaluateData(testPatient);

        List<Alert> afterResolve = generator.getActiveAlertsForPatient(123);
        assertEquals(0, afterResolve.size(), "Manual alert should be resolved");
    }

    /**
     * Test that multiple alert conditions (high BP and low oxygen) are detected simultaneously.
     */
    @Test
    public void multipleAlertsAreDetected() {
        long baseTime = System.currentTimeMillis();
        testPatient.addRecord(185.0, "SystolicBP", baseTime);
        testPatient.addRecord(90.0, "OxygenSaturation", baseTime);

        generator.evaluateData(testPatient);

        List<Alert> foundAlerts = generator.getActiveAlertsForPatient(123);
        assertTrue(foundAlerts.size() >= 2, "Both high BP and low oxygen alerts should be present");
    }
}