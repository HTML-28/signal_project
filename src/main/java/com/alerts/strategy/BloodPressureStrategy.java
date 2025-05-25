package com.alerts.strategy;

import java.util.List;
import com.alerts.Alert;
import com.alerts.AlertSeverity;
import com.alerts.AlertType;
import com.data_management.PatientRecord;

/**
 * Strategy for monitoring blood pressure and generating alerts.
 * Checks for trends and critical thresholds in blood pressure readings.
 */
public class BloodPressureStrategy implements AlertStrategy {

    // Thresholds for blood pressure alerts
    private static final int HIGH_SYSTOLIC_BP_THRESHOLD = 180;
    private static final int LOW_SYSTOLIC_BP_THRESHOLD = 90;
    private static final int HIGH_DIASTOLIC_BP_THRESHOLD = 120;
    private static final int LOW_DIASTOLIC_BP_THRESHOLD = 60;
    private static final int BP_TREND_CHANGE_THRESHOLD = 10;
    private static final int BP_TREND_CONSECUTIVE_READINGS = 3;

    @Override
    public Alert checkAlert(int patientId, List<PatientRecord> records) {
        // Return null if no records to analyze
        if (records == null || records.isEmpty()) {
            return null;
        }

        PatientRecord latest = records.get(records.size() - 1);
        String recordType = latest.getRecordType();
        double value = latest.getMeasurementValue();
        long timestamp = latest.getTimestamp();

        // Systolic BP checks
        if ("SystolicBP".equals(recordType)) {
            if (value >= HIGH_SYSTOLIC_BP_THRESHOLD) {
                // Critical high systolic BP
                return new Alert(
                    patientId,
                    AlertType.HIGH_SYSTOLIC_BP,
                    "Critical high systolic blood pressure: " + value + " mmHg",
                    timestamp,
                    AlertSeverity.CRITICAL
                );
            }
            if (value <= LOW_SYSTOLIC_BP_THRESHOLD) {
                // Critical low systolic BP
                return new Alert(
                    patientId,
                    AlertType.LOW_SYSTOLIC_BP,
                    "Critical low systolic blood pressure: " + value + " mmHg",
                    timestamp,
                    AlertSeverity.HIGH
                );
            }
        }
        // Diastolic BP checks
        else if ("DiastolicBP".equals(recordType)) {
            if (value >= HIGH_DIASTOLIC_BP_THRESHOLD) {
                // Critical high diastolic BP
                return new Alert(
                    patientId,
                    AlertType.HIGH_DIASTOLIC_BP,
                    "Critical high diastolic blood pressure: " + value + " mmHg",
                    timestamp,
                    AlertSeverity.HIGH
                );
            }
            if (value <= LOW_DIASTOLIC_BP_THRESHOLD) {
                // Critical low diastolic BP
                return new Alert(
                    patientId,
                    AlertType.LOW_DIASTOLIC_BP,
                    "Critical low diastolic blood pressure: " + value + " mmHg",
                    timestamp,
                    AlertSeverity.MEDIUM
                );
            }
        }

        // Check for BP trends if enough records are available
        if (records.size() >= BP_TREND_CONSECUTIVE_READINGS) {
            Alert trendAlert = checkBPTrend(patientId, records);
            if (trendAlert != null) {
                return trendAlert;
            }
        }

        // No alert needed
        return null;
    }

    /**
     * Checks for blood pressure trends (increasing or decreasing).
     *
     * @param patientId the ID of the patient
     * @param records the BP records to check
     * @return an Alert if a trend is detected, null otherwise
     */
    private Alert checkBPTrend(int patientId, List<PatientRecord> records) {
        int size = records.size();
        String bpType = records.get(0).getRecordType();

        // Get the last N readings for trend analysis
        List<PatientRecord> recentReadings = records.subList(size - BP_TREND_CONSECUTIVE_READINGS, size);

        boolean increasing = true;
        boolean decreasing = true;

        // Check if each reading changes by more than BP_TREND_CHANGE_THRESHOLD
        for (int i = 1; i < recentReadings.size(); i++) {
            double current = recentReadings.get(i).getMeasurementValue();
            double previous = recentReadings.get(i - 1).getMeasurementValue();

            if (current - previous <= BP_TREND_CHANGE_THRESHOLD) {
                increasing = false;
            }
            if (previous - current <= BP_TREND_CHANGE_THRESHOLD) {
                decreasing = false;
            }
        }

        PatientRecord latest = recentReadings.get(recentReadings.size() - 1);

        if (increasing) {
            return new Alert(
                patientId,
                AlertType.BP_INCREASING_TREND,
                "Increasing trend in " + bpType + " detected over " + BP_TREND_CONSECUTIVE_READINGS + " readings",
                latest.getTimestamp(),
                AlertSeverity.MEDIUM
            );
        }
        if (decreasing) {
            return new Alert(
                patientId,
                AlertType.BP_DECREASING_TREND,
                "Decreasing trend in " + bpType + " detected over " + BP_TREND_CONSECUTIVE_READINGS + " readings",
                latest.getTimestamp(),
                AlertSeverity.MEDIUM
            );
        }
        return null;
    }
}