package com.alerts.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import com.alerts.Alert;
import com.alerts.AlertType;
import com.data_management.PatientRecord;

public class AlertStrategyTest {

    private static final int ID = 123;
    private static final long NOW = System.currentTimeMillis();

    @Test
    @DisplayName("BloodPressureStrategy detects high systolic")
    public void highSystolicTest() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 185.0, "SystolicBP", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect high systolic");
        assertEquals(AlertType.HIGH_SYSTOLIC_BP, alert.getType());
    }

    @Test
    @DisplayName("BloodPressureStrategy detects low systolic")
    public void lowSystolicTest() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 85.0, "SystolicBP", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect low systolic");
        assertEquals(AlertType.LOW_SYSTOLIC_BP, alert.getType());
    }

    @Test
    @DisplayName("BloodPressureStrategy ignores normal systolic")
    public void normalSystolicTest() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 120.0, "SystolicBP", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNull(alert, "Should not alert for normal systolic");
    }

    @Test
    @DisplayName("BloodPressureStrategy detects high diastolic")
    public void highDiastolicTest() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 125.0, "DiastolicBP", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect high diastolic");
        assertEquals(AlertType.HIGH_DIASTOLIC_BP, alert.getType());
    }

    @Test
    @DisplayName("BloodPressureStrategy detects low diastolic")
    public void lowDiastolicTest() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 55.0, "DiastolicBP", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect low diastolic");
        assertEquals(AlertType.LOW_DIASTOLIC_BP, alert.getType());
    }

    @Test
    @DisplayName("BloodPressureStrategy detects increasing BP trend")
    public void bpIncreasingTrendTest() {
        BloodPressureStrategy strategy = new BloodPressureStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 140.0, "SystolicBP", NOW - 3000));
        recs.add(new PatientRecord(ID, 155.0, "SystolicBP", NOW - 2000));
        recs.add(new PatientRecord(ID, 170.0, "SystolicBP", NOW - 1000));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect increasing trend");
        assertEquals(AlertType.BP_INCREASING_TREND, alert.getType());
    }

    @Test
    @DisplayName("HeartRateStrategy detects abnormal ECG")
    public void abnormalECGTest() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            recs.add(new PatientRecord(ID, 70.0, "ECG", NOW - (19 - i) * 1000));
        }
        recs.add(new PatientRecord(ID, 120.0, "ECG", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect abnormal ECG");
        assertEquals(AlertType.ECG_ABNORMAL_PEAK, alert.getType());
    }

    @Test
    @DisplayName("HeartRateStrategy ignores normal ECG")
    public void normalECGTest() {
        HeartRateStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            recs.add(new PatientRecord(ID, 70.0 + (Math.random() * 2 - 1), "ECG", NOW - (20 - i) * 1000));
        }
        Alert alert = strategy.checkAlert(ID, recs);
        assertNull(alert, "Should not alert for normal ECG");
    }

    @Test
    @DisplayName("OxygenSaturationStrategy detects low oxygen")
    public void lowOxygenTest() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 91.0, "OxygenSaturation", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect low oxygen");
        assertEquals(AlertType.LOW_OXYGEN_SATURATION, alert.getType());
    }

    @Test
    @DisplayName("OxygenSaturationStrategy ignores normal oxygen")
    public void normalOxygenTest() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 96.0, "OxygenSaturation", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNull(alert, "Should not alert for normal oxygen");
    }

    @Test
    @DisplayName("OxygenSaturationStrategy detects rapid drop")
    public void rapidOxygenDropTest() {
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> recs = new ArrayList<>();
        recs.add(new PatientRecord(ID, 98.0, "OxygenSaturation", NOW - 500000));
        recs.add(new PatientRecord(ID, 92.0, "OxygenSaturation", NOW));
        Alert alert = strategy.checkAlert(ID, recs);
        assertNotNull(alert, "Should detect rapid oxygen drop");
        assertEquals(AlertType.RAPID_OXYGEN_DROP, alert.getType());
    }
}