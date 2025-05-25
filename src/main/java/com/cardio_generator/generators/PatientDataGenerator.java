package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
/**
 * Class interface for patient data generation
 */
public interface PatientDataGenerator {
    /**
     * Generates a patient's data, then sends it to output.
     *
     * @param patientId ID of the patient
     * @param outputStrategy where the output of the data is
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
