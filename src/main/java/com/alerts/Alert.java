package com.alerts;

import java.time.Instant;

/**
 * Represents an alert triggered for a patient based on their health metrics.
 * Stores alert type, message, timestamp, and severity.
 */
public class Alert {
    // Patient ID associated with this alert
    private final int patientId;
    // Type of alert (see AlertType enum)
    private final AlertType type;
    // Description/message for the alert
    private String message;
    // Time when the alert was triggered (epoch millis)
    private long timestamp;
    // Severity level of the alert (see AlertSeverity enum)
    private AlertSeverity severity;

    /**
     * Constructs a new Alert.
     *
     * @param patientId ID of the patient
     * @param type type of the alert
     * @param message description of the alert
     * @param timestamp time when the alert was triggered (epoch millis)
     * @param severity severity level of the alert
     */
    public Alert(int patientId, AlertType type, String message, long timestamp, AlertSeverity severity) {
        this.patientId = patientId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.severity = severity;
    }

    /**
     * Updates this alert with a new message and timestamp.
     *
     * @param message new alert message
     * @param timestamp new alert timestamp (epoch millis)
     */
    public void updateAlert(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * @return patient ID associated with this alert
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * @return type of this alert
     */
    public AlertType getType() {
        return type;
    }

    /**
     * @return alert message/description
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return timestamp when the alert was triggered (epoch millis)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return severity level of the alert
     */
    public AlertSeverity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity level of the alert.
     *
     * @param severity the new severity level
     */
    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "Alert{patientId=" + patientId +
               ", type=" + type +
               ", message='" + message + "'" +
               ", timestamp=" + Instant.ofEpochMilli(timestamp) +
               ", severity=" + severity + "}";
    }
}
