package com.alerts.strategy;

import java.util.List;
import com.alerts.Alert;
import com.alerts.AlertSeverity;
import com.alerts.AlertType;
import com.data_management.PatientRecord;

/**
 * Strategy for monitoring heart rate (ECG).
 * Detects abnormal peaks in ECG readings using a sliding window approach.
 */
public class HeartRateStrategy implements AlertStrategy {

    // Sliding window size for ECG analysis
    private static final int ECG_WINDOW_SIZE = 20;
    // Threshold: how many standard deviations from mean is considered abnormal
    private static final double ECG_ABNORMAL_THRESHOLD = 2.0;

    @Override
    public Alert checkAlert(int patientId, List<PatientRecord> records) {
        // Not enough data for analysis
        if (records == null || records.size() < ECG_WINDOW_SIZE) {
            return null;
        }

        // Analyze the most recent window of ECG data
        List<PatientRecord> window = records.subList(records.size() - ECG_WINDOW_SIZE, records.size());

        // Calculate mean and standard deviation
        double sum = 0, sumOfSquares = 0;
        for (PatientRecord record : window) {
            double value = record.getMeasurementValue();
            sum += value;
            sumOfSquares += value * value;
        }
        double mean = sum / ECG_WINDOW_SIZE;
        double variance = (sumOfSquares / ECG_WINDOW_SIZE) - (mean * mean);
        double stdDev = Math.sqrt(variance);

        // Check if the latest value is an abnormal peak
        PatientRecord latest = window.get(window.size() - 1);
        double latestValue = latest.getMeasurementValue();

        if (Math.abs(latestValue - mean) > ECG_ABNORMAL_THRESHOLD * stdDev) {
            return new Alert(
                patientId,
                AlertType.ECG_ABNORMAL_PEAK,
                "Abnormal ECG peak detected: " + latestValue + " (exceeds threshold)",
                latest.getTimestamp(),
                AlertSeverity.HIGH
            );
        }

        // No alert needed
        return null;
    }
}