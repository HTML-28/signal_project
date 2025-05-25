package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import java.util.*;
import com.data_management.PatientRecord;
import com.alerts.factory.AlertFactory;

/**
 * The {@code AlertGenerator} class monitors patient data and generates alerts
 * when specific health conditions are met. It uses a {@link DataStorage} instance
 * to access and evaluate patient data against predefined criteria.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;
    // Maps patientId -> (AlertType -> Alert) for currently active alerts
    private final Map<Integer, Map<AlertType, Alert>> activeAlerts = new HashMap<>();
    // Stores historical records for each patient and record type
    private final Map<Integer, Map<String, List<PatientRecord>>> patientRecordHistory = new HashMap<>();

    // Blood pressure thresholds
    private static final int HIGH_SYSTOLIC_BP_THRESHOLD = 180;
    private static final int LOW_SYSTOLIC_BP_THRESHOLD = 90;
    private static final int HIGH_DIASTOLIC_BP_THRESHOLD = 120;
    private static final int LOW_DIASTOLIC_BP_THRESHOLD = 60;
    private static final int BP_TREND_CHANGE_THRESHOLD = 10;
    private static final int BP_TREND_CONSECUTIVE_READINGS = 3;

    // Oxygen saturation thresholds
    private static final double LOW_OXYGEN_THRESHOLD = 92.0;
    private static final double OXYGEN_DROP_THRESHOLD = 5.0;
    private static final long OXYGEN_DROP_TIME_WINDOW_MS = 10 * 60 * 1000; // 10 minutes

    // ECG thresholds
    private static final int ECG_WINDOW_SIZE = 20;
    private static final double ECG_ABNORMAL_THRESHOLD = 2.0;

    /**
     * Constructs an {@code AlertGenerator} with the specified {@code DataStorage}.
     *
     * @param dataStorage the data storage system for patient data access
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data for alert conditions.
     * Triggers or resolves alerts as appropriate.
     *
     * @param patient the patient whose data to evaluate
     */
    public void evaluateData(Patient patient) {
        int patientId = patient.getPatientId();

        // Ensure patient history and active alerts are initialized
        patientRecordHistory.computeIfAbsent(patientId, k -> new HashMap<>());
        activeAlerts.computeIfAbsent(patientId, k -> new HashMap<>());

        // Update patient history with new records from patient object
        updatePatientHistory(patient);

        // Add recent records from storage to history (last 24 hours)
        long now = System.currentTimeMillis();
        long twentyFourHoursAgo = now - (24 * 60 * 60 * 1000);
        List<PatientRecord> recentStorageRecords = dataStorage.getRecords(patientId, twentyFourHoursAgo, now);
        if (!recentStorageRecords.isEmpty()) {
            for (PatientRecord record : recentStorageRecords) {
                String recordType = record.getRecordType();
                patientRecordHistory.get(patientId).computeIfAbsent(recordType, k -> new ArrayList<>());
                List<PatientRecord> typeHistory = patientRecordHistory.get(patientId).get(recordType);
                // Avoid duplicate records by timestamp
                boolean exists = typeHistory.stream().anyMatch(r -> r.getTimestamp() == record.getTimestamp());
                if (!exists) {
                    typeHistory.add(record);
                }
            }
            // Sort all record types by timestamp
            for (List<PatientRecord> typeHistory : patientRecordHistory.get(patientId).values()) {
                typeHistory.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
            }
        }

        // Check all alert types for this patient
        checkBloodPressureAlerts(patientId);
        checkOxygenSaturationAlerts(patientId);
        checkCombinedAlerts(patientId);
        checkECGAlerts(patientId);
        checkManuallyTriggeredAlerts(patientId);

        // Print summary of active alerts for this patient
        Map<AlertType, Alert> patientAlerts = activeAlerts.get(patientId);
        if (patientAlerts != null && !patientAlerts.isEmpty()) {
            System.out.println("Patient #" + patientId + " has " + patientAlerts.size() +
                               " active alert(s): " + patientAlerts.keySet());
        }
    }

    /**
     * Updates the patient's record history with new data from the Patient object.
     *
     * @param patient the patient whose history to update
     */
    private void updatePatientHistory(Patient patient) {
        int patientId = patient.getPatientId();
        Map<String, List<PatientRecord>> recordsByType = patient.getRecordsByType();

        for (Map.Entry<String, List<PatientRecord>> entry : recordsByType.entrySet()) {
            String recordType = entry.getKey();
            List<PatientRecord> records = entry.getValue();
            if (!records.isEmpty()) {
                patientRecordHistory.get(patientId).computeIfAbsent(recordType, k -> new ArrayList<>());
                List<PatientRecord> history = patientRecordHistory.get(patientId).get(recordType);

                // Limit history size to last 100 records
                if (history.size() > 100) {
                    history = history.subList(history.size() - 100, history.size());
                }

                // Add new records if not already present (by timestamp)
                for (PatientRecord record : records) {
                    boolean exists = history.stream().anyMatch(r -> r.getTimestamp() == record.getTimestamp());
                    if (!exists) {
                        history.add(record);
                    }
                }
                // Sort by timestamp
                history.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
                patientRecordHistory.get(patientId).put(recordType, history);
            }
        }
    }

    /**
     * Checks for blood pressure related alerts (high/low/trend).
     *
     * @param patientId the ID of the patient to check
     */
    private void checkBloodPressureAlerts(int patientId) {
        Map<String, List<PatientRecord>> recordsByType = patientRecordHistory.get(patientId);
        if (recordsByType == null) return;

        List<PatientRecord> systolicRecords = recordsByType.get("SystolicBP");
        List<PatientRecord> diastolicRecords = recordsByType.get("DiastolicBP");

        // Systolic BP checks
        if (systolicRecords != null && !systolicRecords.isEmpty()) {
            PatientRecord latestSystolic = systolicRecords.get(systolicRecords.size() - 1);
            double systolicValue = latestSystolic.getMeasurementValue();
            long timestamp = latestSystolic.getTimestamp();
            // High systolic BP alert check (uses factory)
            if (systolicValue >= HIGH_SYSTOLIC_BP_THRESHOLD) {
                AlertFactory factory = AlertFactory.getFactory("bloodpressure");
                Alert alert = factory.createAlert(patientId, "high_systolic", timestamp, systolicValue);
                triggerAlert(alert);
            } else {
                resolveAlert(patientId, AlertType.HIGH_SYSTOLIC_BP);
            }

            // Low systolic BP alert
            if (systolicValue <= LOW_SYSTOLIC_BP_THRESHOLD) {
                triggerAlert(new Alert(
                    patientId, AlertType.LOW_SYSTOLIC_BP,
                    "Critical low systolic blood pressure: " + systolicValue + " mmHg",
                    timestamp, AlertSeverity.HIGH));
            } else {
                resolveAlert(patientId, AlertType.LOW_SYSTOLIC_BP);
            }

            // Check for systolic BP trend if enough readings
            if (systolicRecords.size() >= BP_TREND_CONSECUTIVE_READINGS) {
                checkBPTrend(patientId, systolicRecords, "systolic");
            }
        }

        // Diastolic BP checks
        if (diastolicRecords != null && !diastolicRecords.isEmpty()) {
            PatientRecord latestDiastolic = diastolicRecords.get(diastolicRecords.size() - 1);
            double diastolicValue = latestDiastolic.getMeasurementValue();
            long timestamp = latestDiastolic.getTimestamp();

            // High diastolic BP alert
            if (diastolicValue >= HIGH_DIASTOLIC_BP_THRESHOLD) {
                triggerAlert(new Alert(
                    patientId, AlertType.HIGH_DIASTOLIC_BP,
                    "Critical high diastolic blood pressure: " + diastolicValue + " mmHg",
                    timestamp, AlertSeverity.HIGH));
            } else {
                resolveAlert(patientId, AlertType.HIGH_DIASTOLIC_BP);
            }

            // Low diastolic BP alert
            if (diastolicValue <= LOW_DIASTOLIC_BP_THRESHOLD) {
                triggerAlert(new Alert(
                    patientId, AlertType.LOW_DIASTOLIC_BP,
                    "Critical low diastolic blood pressure: " + diastolicValue + " mmHg",
                    timestamp, AlertSeverity.MEDIUM));
            } else {
                resolveAlert(patientId, AlertType.LOW_DIASTOLIC_BP);
            }

            // Check for diastolic BP trend if enough readings
            if (diastolicRecords.size() >= BP_TREND_CONSECUTIVE_READINGS) {
                checkBPTrend(patientId, diastolicRecords, "diastolic");
            }
        }
    }

    /**
     * Checks for blood pressure trends (increasing or decreasing) over recent readings.
     *
     * @param patientId the ID of the patient
     * @param records the BP records to check
     * @param bpType "systolic" or "diastolic"
     */
    private void checkBPTrend(int patientId, List<PatientRecord> records, String bpType) {
        int size = records.size();
        List<PatientRecord> recentReadings = records.subList(size - BP_TREND_CONSECUTIVE_READINGS, size);

        boolean increasing = true;
        boolean decreasing = true;

        // Check if all consecutive readings are increasing or decreasing by threshold
        for (int i = 1; i < recentReadings.size(); i++) {
            double current = recentReadings.get(i).getMeasurementValue();
            double previous = recentReadings.get(i - 1).getMeasurementValue();
            if (current - previous <= BP_TREND_CHANGE_THRESHOLD) increasing = false;
            if (previous - current <= BP_TREND_CHANGE_THRESHOLD) decreasing = false;
        }

        PatientRecord latest = recentReadings.get(recentReadings.size() - 1);

        if (increasing) {
            triggerAlert(new Alert(
                patientId, AlertType.BP_INCREASING_TREND,
                "Increasing trend in " + bpType + " blood pressure detected over " + BP_TREND_CONSECUTIVE_READINGS + " readings",
                latest.getTimestamp(), AlertSeverity.MEDIUM));
        } else {
            resolveAlert(patientId, AlertType.BP_INCREASING_TREND);
        }

        if (decreasing) {
            triggerAlert(new Alert(
                patientId, AlertType.BP_DECREASING_TREND,
                "Decreasing trend in " + bpType + " blood pressure detected over " + BP_TREND_CONSECUTIVE_READINGS + " readings",
                latest.getTimestamp(), AlertSeverity.MEDIUM));
        } else {
            resolveAlert(patientId, AlertType.BP_DECREASING_TREND);
        }
    }

    /**
     * Checks for oxygen saturation related alerts (low or rapid drop).
     *
     * @param patientId the ID of the patient to check
     */
    private void checkOxygenSaturationAlerts(int patientId) {
        Map<String, List<PatientRecord>> recordsByType = patientRecordHistory.get(patientId);
        if (recordsByType == null) return;

        List<PatientRecord> oxygenRecords = recordsByType.get("OxygenSaturation");
        if (oxygenRecords != null && !oxygenRecords.isEmpty()) {
            PatientRecord latestOxygen = oxygenRecords.get(oxygenRecords.size() - 1);
            double oxygenValue = latestOxygen.getMeasurementValue();
            long timestamp = latestOxygen.getTimestamp();
            // Check for low oxygen saturation (uses factory)
            if (oxygenValue < LOW_OXYGEN_THRESHOLD) {
                AlertFactory factory = AlertFactory.getFactory("bloodoxygen");
                Alert alert = factory.createAlert(patientId, "low_saturation", timestamp, oxygenValue);
                triggerAlert(alert);
            } else {
                resolveAlert(patientId, AlertType.LOW_OXYGEN_SATURATION);
            }

            // Check for rapid drop in oxygen saturation
            if (oxygenRecords.size() >= 2) {
                checkOxygenRapidDrop(patientId, oxygenRecords);
            }
        }
    }

    /**
     * Checks for rapid drops in oxygen saturation within a time window.
     *
     * @param patientId the ID of the patient
     * @param records the oxygen saturation records to check
     */
    private void checkOxygenRapidDrop(int patientId, List<PatientRecord> records) {
        PatientRecord latest = records.get(records.size() - 1);
        double latestValue = latest.getMeasurementValue();
        long latestTime = latest.getTimestamp();

        // Look back for a drop >= threshold within the time window
        for (int i = records.size() - 2; i >= 0; i--) {
            PatientRecord earlier = records.get(i);
            long earlierTime = earlier.getTimestamp();
            if (latestTime - earlierTime > OXYGEN_DROP_TIME_WINDOW_MS) break;
            double earlierValue = earlier.getMeasurementValue();
            double drop = earlierValue - latestValue;
            if (drop >= OXYGEN_DROP_THRESHOLD) {
                triggerAlert(new Alert(
                    patientId, AlertType.RAPID_OXYGEN_DROP,
                    "Rapid drop in oxygen saturation of " + String.format("%.1f", drop) + "% within 10 minutes",
                    latestTime, AlertSeverity.HIGH));
                return;
            }
        }
        resolveAlert(patientId, AlertType.RAPID_OXYGEN_DROP);
    }

    /**
     * Checks for combined alerts such as Hypotensive Hypoxemia (low BP and low O2).
     *
     * @param patientId the ID of the patient to check
     */
    private void checkCombinedAlerts(int patientId) {
        Map<String, List<PatientRecord>> recordsByType = patientRecordHistory.get(patientId);
        if (recordsByType == null) return;

        List<PatientRecord> systolicRecords = recordsByType.get("SystolicBP");
        List<PatientRecord> oxygenRecords = recordsByType.get("OxygenSaturation");

        if (systolicRecords != null && !systolicRecords.isEmpty() &&
            oxygenRecords != null && !oxygenRecords.isEmpty()) {

            PatientRecord latestSystolic = systolicRecords.get(systolicRecords.size() - 1);
            PatientRecord latestOxygen = oxygenRecords.get(oxygenRecords.size() - 1);

            double systolicValue = latestSystolic.getMeasurementValue();
            double oxygenValue = latestOxygen.getMeasurementValue();

            // Trigger combined alert if both BP and O2 are low
            if (systolicValue < LOW_SYSTOLIC_BP_THRESHOLD && oxygenValue < LOW_OXYGEN_THRESHOLD) {
                triggerAlert(new Alert(
                    patientId, AlertType.HYPOTENSIVE_HYPOXEMIA,
                    "Critical condition: Hypotensive Hypoxemia detected - Low blood pressure (" +
                        systolicValue + " mmHg) and low oxygen saturation (" + oxygenValue + "%)",
                    Math.max(latestSystolic.getTimestamp(), latestOxygen.getTimestamp()),
                    AlertSeverity.CRITICAL));
            } else {
                resolveAlert(patientId, AlertType.HYPOTENSIVE_HYPOXEMIA);
            }
        }
    }

    /**
     * Checks for ECG abnormalities using a sliding window approach.
     *
     * @param patientId the ID of the patient to check
     */
    private void checkECGAlerts(int patientId) {
        Map<String, List<PatientRecord>> recordsByType = patientRecordHistory.get(patientId);
        if (recordsByType == null) return;

        List<PatientRecord> ecgRecords = recordsByType.get("ECG");
        if (ecgRecords != null && ecgRecords.size() >= ECG_WINDOW_SIZE) {
            List<PatientRecord> window = ecgRecords.subList(ecgRecords.size() - ECG_WINDOW_SIZE, ecgRecords.size());
            double sum = 0, sumOfSquares = 0;
            for (PatientRecord record : window) {
                double value = record.getMeasurementValue();
                sum += value;
                sumOfSquares += value * value;
            }
            double mean = sum / ECG_WINDOW_SIZE;
            double variance = (sumOfSquares / ECG_WINDOW_SIZE) - (mean * mean);
            double stdDev = Math.sqrt(variance);

            PatientRecord latest = window.get(window.size() - 1);
            double latestValue = latest.getMeasurementValue();
            long latestTime = latest.getTimestamp();

            // Check for abnormal peaks in ECG (uses factory)
            if (Math.abs(latestValue - mean) > ECG_ABNORMAL_THRESHOLD * stdDev) {
                AlertFactory factory = AlertFactory.getFactory("ecg");
                Alert alert = factory.createAlert(patientId, "abnormal_peak", latestTime, latestValue);
                triggerAlert(alert);
            } else {
                resolveAlert(patientId, AlertType.ECG_ABNORMAL_PEAK);
            }
        }
    }

    /**
     * Checks for manually triggered alerts (from patient or staff).
     *
     * @param patientId the ID of the patient to check
     */
    private void checkManuallyTriggeredAlerts(int patientId) {
        Map<String, List<PatientRecord>> recordsByType = patientRecordHistory.get(patientId);
        if (recordsByType == null) return;

        List<PatientRecord> alertRecords = recordsByType.get("Alert");
        if (alertRecords != null && !alertRecords.isEmpty()) {
            PatientRecord latestAlert = alertRecords.get(alertRecords.size() - 1);
            String alertStatus = latestAlert.getAdditionalInfo();

            if ("triggered".equalsIgnoreCase(alertStatus)) {
                triggerAlert(new Alert(
                    patientId, AlertType.MANUAL_TRIGGER,
                    "Manual alert triggered by patient or staff",
                    latestAlert.getTimestamp(), AlertSeverity.HIGH));
            } else if ("resolved".equalsIgnoreCase(alertStatus)) {
                resolveAlert(patientId, AlertType.MANUAL_TRIGGER);
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. Updates existing alert if present.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        int patientId = alert.getPatientId();
        AlertType alertType = alert.getType();

        Map<AlertType, Alert> patientAlerts = activeAlerts.get(patientId);
        if (patientAlerts == null) {
            patientAlerts = new HashMap<>();
            activeAlerts.put(patientId, patientAlerts);
        }

        // Update existing alert or add new alert
        if (patientAlerts.containsKey(alertType)) {
            Alert existingAlert = patientAlerts.get(alertType);
            existingAlert.updateAlert(alert.getMessage(), alert.getTimestamp());
        } else {
            patientAlerts.put(alertType, alert);
            System.out.println("ALERT TRIGGERED: " + alert);
            // Additional notification logic can be added here
        }
    }

    /**
     * Resolves an active alert for a patient.
     *
     * @param patientId ID of the patient
     * @param alertType type of alert to resolve
     */
    private void resolveAlert(int patientId, AlertType alertType) {
        Map<AlertType, Alert> patientAlerts = activeAlerts.get(patientId);
        if (patientAlerts != null && patientAlerts.containsKey(alertType)) {
            Alert alert = patientAlerts.remove(alertType);
            System.out.println("ALERT RESOLVED: " + alert);
            // Additional resolution logic can be added here
        }
    }

    /**
     * Gets all active alerts for a patient.
     *
     * @param patientId the patient ID
     * @return list of active alerts or empty list if none
     */
    public List<Alert> getActiveAlertsForPatient(int patientId) {
        Map<AlertType, Alert> patientAlerts = activeAlerts.get(patientId);
        return patientAlerts != null ? new ArrayList<>(patientAlerts.values()) : new ArrayList<>();
    }

    /**
     * Gets all active alerts in the system.
     *
     * @return list of all active alerts
     */
    public List<Alert> getAllActiveAlerts() {
        List<Alert> allAlerts = new ArrayList<>();
        for (Map<AlertType, Alert> patientAlerts : activeAlerts.values()) {
            allAlerts.addAll(patientAlerts.values());
        }
        return allAlerts;
    }
}
