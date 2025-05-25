package com.cardio_generator.generators;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import com.cardio_generator.outputs.OutputStrategy;
import java.util.ArrayList;
import java.util.List;

public class ECGDataGeneratorTest {

    private static final int PATIENTS = 10;
    private ECGDataGenerator ecgGen;

    @BeforeEach
    public void setup() {
        ecgGen = new ECGDataGenerator(PATIENTS);
    }

    @Test
    @DisplayName("Constructor creates ECG generator")
    public void constructorTest() {
        assertNotNull(ecgGen, "Should instantiate ECG generator");
    }

    @Test
    @DisplayName("Generate produces ECG values")
    public void generateProducesECG() {
        RecordingOutputStrategy output = new RecordingOutputStrategy();
        int id = 5;
        ecgGen.generate(id, output);

        boolean found = false;
        for (OutputRecord rec : output.getRecords()) {
            if (rec.patientId == id && "ECG".equals(rec.recordType)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should output ECG");
    }

    @Test
    @DisplayName("ECG values are within physiological range")
    public void valueRangesTest() {
        RecordingOutputStrategy output = new RecordingOutputStrategy();
        for (int id = 1; id <= PATIENTS; id++) {
            ecgGen.generate(id, output);
        }
        for (OutputRecord rec : output.getRecords()) {
            if ("ECG".equals(rec.recordType)) {
                double v = Double.parseDouble(rec.value);
                assertTrue(v >= -1 && v <= 1, "ECG in range: " + v);
            }
        }
    }

    // Helper for capturing output
    private static class RecordingOutputStrategy implements OutputStrategy {
        private List<OutputRecord> records = new ArrayList<>();
        @Override
        public void output(int patientId, long timestamp, String recordType, String value) {
            records.add(new OutputRecord(patientId, timestamp, recordType, value));
        }
        public List<OutputRecord> getRecords() { return records; }
    }

    // Simple record structure
    private static class OutputRecord {
        public final int patientId;
        public final long timestamp;
        public final String recordType;
        public final String value;
        public OutputRecord(int patientId, long timestamp, String recordType, String value) {
            this.patientId = patientId;
            this.timestamp = timestamp;
            this.recordType = recordType;
            this.value = value;
        }
    }
}