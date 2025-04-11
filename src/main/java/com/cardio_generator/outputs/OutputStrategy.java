package com.cardio_generator.outputs;

//changed to camelCase
public interface OutputStrategy {
    void output(int patientId, long timeStamp, String label, String data);
}
