package com.cardio_generator.outputs;

/**
 * Patient data outputting interface
 */
public interface OutputStrategy {

    /**
     * Transfers pateint's data to output.
     *
     * @param patientId patient ID number
     * @param timestamp time when the data was created
     * @param label label for the type of data
     * @param data value of the data
     */
    void output(int patientId, long timestamp, String label, String data);
}
