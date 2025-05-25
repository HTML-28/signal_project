package com.alerts.factory;

import com.alerts.Alert;

/**
 * Abstract factory for creating Alert instances based on specific conditions.
 * Concrete subclasses implement the creation logic for each alert category.
 */
public abstract class AlertFactory {

    /**
     * Factory method to create an Alert for a given patient and condition.
     *
     * @param patientId the patient's ID
     * @param condition the specific alert trigger or medical condition
     * @param timestamp when the alert condition was detected
     * @param value the measurement value that triggered the alert
     * @return an Alert instance for the specified condition
     */
    public abstract Alert createAlert(int patientId, String condition, long timestamp, double value);

    /**
     * Returns the appropriate AlertFactory for the given alert category.
     *
     * @param category the alert category (e.g., "bloodpressure", "bloodoxygen", "ecg")
     * @return a concrete AlertFactory implementation
     * @throws IllegalArgumentException if the category is unknown
     */
    public static AlertFactory getFactory(String category) {
        switch (category.toLowerCase()) {
            case "bloodpressure":
                return new BloodPressureAlertFactory();
            case "bloodoxygen":
                return new BloodOxygenAlertFactory();
            case "ecg":
                return new ECGAlertFactory();
            default:
                throw new IllegalArgumentException("Unknown alert category: " + category);
        }
    }
}