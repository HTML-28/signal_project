package com.alerts.strategy;

import java.util.List;
import com.alerts.Alert;
import com.alerts.AlertSeverity;
import com.alerts.AlertType;
import com.data_management.PatientRecord;

/**
 * Strategy for monitoring oxygen saturation.
 * Observes oxygen levels for critical drops and rapid decreases.
 */
public class OxygenSaturationStrategy implements AlertStrategy {

    // Thresholds for oxygen saturation alerts
    private static final double LOW_OXYGEN_THRESHOLD = 92.0;
    private static final double OXYGEN_DROP_THRESHOLD = 5.0;
    private static final long OXYGEN_DROP_TIME_WINDOW_MS = 10 * 60 * 1000; // 10 minutes

    @Override
    public Alert checkAlert(int patientId, List<PatientRecord> records) {
        // Return null if no records to analyze
        if (records == null || records.isEmpty()) {
            return null;
        }

        PatientRecord latest = records.get(records.size() - 1);
        double latestValue = latest.getMeasurementValue();
        long latestTime = latest.getTimestamp();

        // Low oxygen saturation alert
        if (latestValue < LOW_OXYGEN_THRESHOLD) {
            return new Alert(
                patientId,
                AlertType.LOW_OXYGEN_SATURATION,
                "Low oxygen saturation: " + latestValue + "%",
                latestTime,
                AlertSeverity.HIGH
            );
        }

        // Check for rapid drop if enough records exist
        if (records.size() >= 2) {
            Alert rapidDropAlert = checkRapidDrop(patientId, records);
            if (rapidDropAlert != null) {
                return rapidDropAlert;
            }
        }

        // No alert needed
        return null;
    }

    /**
     * Checks for rapid drops in oxygen saturation within a time window.
     *
     * @param patientId the ID of the patient
     * @param records the oxygen saturation records to check
     * @return an Alert if a rapid drop is detected, null otherwise
     */
    private Alert checkRapidDrop(int patientId, List<PatientRecord> records) {
        PatientRecord latest = records.get(records.size() - 1);
        double latestValue = latest.getMeasurementValue();
        long latestTime = latest.getTimestamp();

        // Look back for a drop >= threshold within the time window
        for (int i = records.size() - 2; i >= 0; i--) {
            PatientRecord earlier = records.get(i);
            long earlierTime = earlier.getTimestamp();

            // Only check records within the time window
            if (latestTime - earlierTime > OXYGEN_DROP_TIME_WINDOW_MS) {
                break;
            }

            double earlierValue = earlier.getMeasurementValue();
            double drop = earlierValue - latestValue;

            if (drop >= OXYGEN_DROP_THRESHOLD) {
                return new Alert(
                    patientId,
                    AlertType.RAPID_OXYGEN_DROP,
                    "Rapid drop in oxygen saturation of " + String.format("%.1f", drop) + "% within 10 minutes",
                    latestTime,
                    AlertSeverity.HIGH
                );
            }
        }
        return null;
    }
}