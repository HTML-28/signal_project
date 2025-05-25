package com.alerts;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.alerts.decorator.PriorityAlertDecorator;
import com.alerts.decorator.RepeatedAlertDecorator;
import com.alerts.factory.AlertFactory;
import com.alerts.strategy.BloodPressureStrategy;

public class IntegrationTest {

    private DataStorage storage;
    private AlertGenerator generator;

    @BeforeEach
    public void initialize() {
        // Ensure singleton is reset before each test
        storage = DataStorage.getInstance();
        storage.clearAllData();

        // Instantiate alert generator with fresh storage
        generator = new AlertGenerator(storage);
    }

    @Test
    @DisplayName("Integration: Factory, Strategy, Decorator patterns")
    public void integrationPatternsTest() {
        // Simulate a patient
        Patient testPatient = new Patient(123);

        // Create alert using factory
        AlertFactory bpFactory = AlertFactory.getFactory("bloodpressure");
        Alert alertFromFactory = bpFactory.createAlert(123, "high_systolic", System.currentTimeMillis(), 185.0);

        // Add a record and use strategy to check for alert
        BloodPressureStrategy bpStrategy = new BloodPressureStrategy();
        testPatient.addRecord(185.0, "SystolicBP", System.currentTimeMillis());
        List<PatientRecord> patientRecords = testPatient.getAllRecords();
        Alert alertFromStrategy = bpStrategy.checkAlert(testPatient.getPatientId(), patientRecords);

        // Assert that both factory and strategy produce the same alert type
        assertNotNull(alertFromStrategy, "Strategy should generate alert");
        assertEquals(alertFromFactory.getType(), alertFromStrategy.getType(),
                "Alert types from factory and strategy should match");

        // Decorate the alert with priority
        Alert decoratedAlert = new PriorityAlertDecorator(alertFromFactory, "Patient has cardiac history");

        // Check decorator's effect
        assertEquals(AlertSeverity.CRITICAL, decoratedAlert.getSeverity(),
                "Priority decorator should escalate severity to CRITICAL");
        assertTrue(decoratedAlert.getMessage().contains("PRIORITY"),
                "Priority decorator should add PRIORITY to message");

        // Evaluate data and trigger alerts
        generator.evaluateData(testPatient);

        // Validate that alert is active in the system
        List<Alert> alerts = generator.getActiveAlertsForPatient(123);
        assertFalse(alerts.isEmpty(), "Active alerts should exist for patient");
        assertEquals(AlertType.HIGH_SYSTOLIC_BP, alerts.get(0).getType(),
                "First alert should be high systolic BP");
    }
}