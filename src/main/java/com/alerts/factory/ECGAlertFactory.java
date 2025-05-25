package com.alerts.factory;

import com.alerts.Alert;
import com.alerts.AlertSeverity;
import com.alerts.AlertType;

/**
 * Factory for creating ECG related alerts.
 */
public class ECGAlertFactory extends AlertFactory {

    /**
     * Creates an Alert for ECG related conditions.
     *
     * @param patientId the patient's ID
     * @param condition the specific ECG alert trigger
     * @param timestamp when the alert was detected
     * @param value the ECG measurement value
     * @return an Alert instance for the specified condition
     */
    @Override
    public Alert createAlert(int patientId, String condition, long timestamp, double value) {
        switch (condition) {
            case "abnormal_peak":
                return new Alert(
                    patientId,
                    AlertType.ECG_ABNORMAL_PEAK,
                    "Abnormal ECG peak detected: " + value + " (exceeds normal threshold)",
                    timestamp,
                    AlertSeverity.HIGH
                );
            default:
                throw new IllegalArgumentException("Unknown ECG condition: " + condition);
        }
    }
}