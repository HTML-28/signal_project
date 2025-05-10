package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Randomly activates or resolves patient alerts.
 */
public class AlertGenerator implements PatientDataGenerator {

    public static final Random randomGenerator = new Random();
    // Change the name to lower camel case
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Creares states of alert for every patient.
     *
     * @param patientCount total number of patients
     */
    public AlertGenerator(int patientCount) {
        // Set the name to the right one
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Randomly activates or resolves an alert and outputs it.
     *
     * @param patientId patient's ID
     * @param outputStrategy where the alert data is sent
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    // Set the name to the right one
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                double Lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-Lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                 // set the name to be correct
                 alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
