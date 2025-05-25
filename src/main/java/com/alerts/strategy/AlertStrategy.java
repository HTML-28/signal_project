package com.alerts.strategy;

import java.util.List;
import com.alerts.Alert;
import com.data_management.PatientRecord;

/**
 * Strategy interface for alert checking algorithms.
 * Implementations define how to determine if an alert should be triggered
 * based on a patient's data records.
 */
public interface AlertStrategy {

    /**
     * Checks if an alert should be triggered for a patient based on their records.
     *
     * @param patientId the ID of the patient
     * @param records the list of patient records to analyze
     * @return an Alert object if an alert should be triggered, or null if no alert is needed
     */
    Alert checkAlert(int patientId, List<PatientRecord> records);
}