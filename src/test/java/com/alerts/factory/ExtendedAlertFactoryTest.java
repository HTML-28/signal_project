package com.alerts.factory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.alerts.Alert;
import com.alerts.AlertSeverity;
import com.alerts.AlertType;

public class ExtendedAlertFactoryTest {

    @Test
    @DisplayName("Factory creates high diastolic BP alert")
    public void highDiastolicBPTest() {
        AlertFactory factory = AlertFactory.getFactory("bloodpressure");
        Alert alert = factory.createAlert(123, "high_diastolic", System.currentTimeMillis(), 125.0);

        assertEquals(AlertType.HIGH_DIASTOLIC_BP, alert.getType(), "Type should be HIGH_DIASTOLIC_BP");
        assertEquals(AlertSeverity.HIGH, alert.getSeverity(), "Severity should be HIGH");
        assertTrue(alert.getMessage().contains("125.0 mmHg"), "Message should mention value");
    }

    @Test
    @DisplayName("Factory creates low diastolic BP alert")
    public void lowDiastolicBPTest() {
        AlertFactory factory = AlertFactory.getFactory("bloodpressure");
        Alert alert = factory.createAlert(123, "low_diastolic", System.currentTimeMillis(), 55.0);

        assertEquals(AlertType.LOW_DIASTOLIC_BP, alert.getType(), "Type should be LOW_DIASTOLIC_BP");
        assertEquals(AlertSeverity.MEDIUM, alert.getSeverity(), "Severity should be MEDIUM");
    }

    @Test
    @DisplayName("Factory creates BP trend alerts")
    public void bpTrendAlertsTest() {
        AlertFactory factory = AlertFactory.getFactory("bloodpressure");

        Alert inc = factory.createAlert(123, "increasing_trend", System.currentTimeMillis(), 150.0);
        assertEquals(AlertType.BP_INCREASING_TREND, inc.getType(), "Type should be BP_INCREASING_TREND");

        Alert dec = factory.createAlert(123, "decreasing_trend", System.currentTimeMillis(), 110.0);
        assertEquals(AlertType.BP_DECREASING_TREND, dec.getType(), "Type should be BP_DECREASING_TREND");
    }
}