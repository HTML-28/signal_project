package com.alerts.decorator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.alerts.Alert;
import com.alerts.AlertSeverity;
import com.alerts.AlertType;

public class AlertDecoratorTest {

    @Test
    @DisplayName("PriorityAlertDecorator escalates severity and modifies message")
    public void priorityDecoratorTest() {
        // Setup a base alert
        Alert original = new Alert(123, AlertType.HIGH_SYSTOLIC_BP,
                "High systolic blood pressure: 185.0 mmHg",
                System.currentTimeMillis(), AlertSeverity.MEDIUM);

        // Decorate with priority
        String priorityReason = "Patient has cardiac history";
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(original, priorityReason);

        // Check severity escalation and message content
        assertEquals(AlertSeverity.HIGH, decorated.getSeverity(), "Should escalate to HIGH severity");
        assertTrue(decorated.getMessage().contains("PRIORITY"), "Should mention PRIORITY in message");
        assertTrue(decorated.getMessage().contains(priorityReason), "Should include reason in message");
        assertEquals(original.getPatientId(), decorated.getPatientId(), "Patient ID should match");
        assertEquals(original.getType(), decorated.getType(), "Alert type should match");
    }

    @Test
    @DisplayName("RepeatedAlertDecorator modifies message and preserves alert info")
    public void repeatedDecoratorTest() {
        // Setup a base alert
        Alert alert = new Alert(456, AlertType.LOW_OXYGEN_SATURATION,
                "Low oxygen saturation: 90.0%",
                System.currentTimeMillis(), AlertSeverity.HIGH);

        // Decorate with repeat
        long repeatInterval = 1000;
        int repeatCount = 3;
        RepeatedAlertDecorator repeated = new RepeatedAlertDecorator(alert, repeatInterval, repeatCount);

        // Check message and alert info
        assertTrue(repeated.getMessage().contains("[REPEAT"), "Should indicate repeat in message");
        assertEquals(alert.getPatientId(), repeated.getPatientId(), "Patient ID should match");
        assertEquals(alert.getType(), repeated.getType(), "Alert type should match");

        // Cancel timer to avoid resource leak
        repeated.cancelRepeats();
    }

    @Test
    @DisplayName("PriorityAlertDecorator escalates all severity levels correctly")
    public void severityEscalationTest() {
        // Escalate LOW to MEDIUM
        Alert low = new Alert(123, AlertType.LOW_DIASTOLIC_BP, "Test",
                System.currentTimeMillis(), AlertSeverity.LOW);
        PriorityAlertDecorator lowEscalated = new PriorityAlertDecorator(low, "Test");
        assertEquals(AlertSeverity.MEDIUM, lowEscalated.getSeverity());

        // Escalate MEDIUM to HIGH
        Alert medium = new Alert(123, AlertType.LOW_DIASTOLIC_BP, "Test",
                System.currentTimeMillis(), AlertSeverity.MEDIUM);
        PriorityAlertDecorator mediumEscalated = new PriorityAlertDecorator(medium, "Test");
        assertEquals(AlertSeverity.HIGH, mediumEscalated.getSeverity());

        // Escalate HIGH to CRITICAL
        Alert high = new Alert(123, AlertType.LOW_DIASTOLIC_BP, "Test",
                System.currentTimeMillis(), AlertSeverity.HIGH);
        PriorityAlertDecorator highEscalated = new PriorityAlertDecorator(high, "Test");
        assertEquals(AlertSeverity.CRITICAL, highEscalated.getSeverity());

        // CRITICAL remains CRITICAL
        Alert critical = new Alert(123, AlertType.LOW_DIASTOLIC_BP, "Test",
                System.currentTimeMillis(), AlertSeverity.CRITICAL);
        PriorityAlertDecorator criticalEscalated = new PriorityAlertDecorator(critical, "Test");
        assertEquals(AlertSeverity.CRITICAL, criticalEscalated.getSeverity());
    }

    @Test
    @DisplayName("Multiple decorators combine their effects")
    public void combinedDecoratorsTest() {
        // Base alert
        Alert alert = new Alert(123, AlertType.HIGH_SYSTOLIC_BP,
                "High systolic blood pressure: 185.0 mmHg",
                System.currentTimeMillis(), AlertSeverity.MEDIUM);

        // Apply priority, then repeat decorators
        PriorityAlertDecorator priority = new PriorityAlertDecorator(alert, "Patient history");
        RepeatedAlertDecorator combined = new RepeatedAlertDecorator(priority, 1000, 2);

        // Check combined effects
        assertEquals(AlertSeverity.HIGH, combined.getSeverity());
        assertTrue(combined.getMessage().contains("PRIORITY"));
        assertTrue(combined.getMessage().contains("[REPEAT"));

        // Cancel timer
        combined.cancelRepeats();
    }
}