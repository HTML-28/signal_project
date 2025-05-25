package com.alerts.factory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.alerts.Alert;
import com.alerts.AlertType;
import com.alerts.AlertSeverity;

public class AlertFactoryTest {

    @Test
    @DisplayName("BloodPressureAlertFactory produces correct alerts")
    public void bloodPressureFactoryTest() {
        AlertFactory factory = AlertFactory.getFactory("bloodpressure");

        // High systolic
        Alert highSys = factory.createAlert(123, "high_systolic", System.currentTimeMillis(), 185.0);
        assertEquals(AlertType.HIGH_SYSTOLIC_BP, highSys.getType());
        assertEquals(AlertSeverity.CRITICAL, highSys.getSeverity());

        // Low systolic
        Alert lowSys = factory.createAlert(123, "low_systolic", System.currentTimeMillis(), 85.0);
        assertEquals(AlertType.LOW_SYSTOLIC_BP, lowSys.getType());
        assertEquals(AlertSeverity.HIGH, lowSys.getSeverity());

        // Invalid condition should throw
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createAlert(123, "invalid_condition", System.currentTimeMillis(), 120.0);
        });
    }

    @Test
    @DisplayName("BloodOxygenAlertFactory produces correct alerts")
    public void bloodOxygenFactoryTest() {
        AlertFactory factory = AlertFactory.getFactory("bloodoxygen");

        Alert lowO2 = factory.createAlert(123, "low_saturation", System.currentTimeMillis(), 90.0);
        assertEquals(AlertType.LOW_OXYGEN_SATURATION, lowO2.getType());
        assertEquals(AlertSeverity.HIGH, lowO2.getSeverity());
    }

    @Test
    @DisplayName("ECGAlertFactory produces correct alerts")
    public void ecgFactoryTest() {
        AlertFactory factory = AlertFactory.getFactory("ecg");

        Alert ecg = factory.createAlert(123, "abnormal_peak", System.currentTimeMillis(), 120.0);
        assertEquals(AlertType.ECG_ABNORMAL_PEAK, ecg.getType());
        assertEquals(AlertSeverity.HIGH, ecg.getSeverity());
    }

    @Test
    @DisplayName("AlertFactory returns correct factory type")
    public void factorySelectionTest() {
        assertTrue(AlertFactory.getFactory("bloodpressure") instanceof BloodPressureAlertFactory);
        assertTrue(AlertFactory.getFactory("bloodoxygen") instanceof BloodOxygenAlertFactory);
        assertTrue(AlertFactory.getFactory("ecg") instanceof ECGAlertFactory);

        assertThrows(IllegalArgumentException.class, () -> {
            AlertFactory.getFactory("unknown");
        });
    }
}