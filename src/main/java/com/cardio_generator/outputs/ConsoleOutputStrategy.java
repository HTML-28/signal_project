package com.cardio_generator.outputs;

public class ConsoleOutputStrategy implements OutputStrategy {
    @Override
    public void output(int patientId, long timeStamp, String label, String data) {
        System.out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timeStamp, label, data);
    }
}
