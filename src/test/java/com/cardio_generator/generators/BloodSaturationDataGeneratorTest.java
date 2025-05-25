package com.cardio_generator.generators;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import com.cardio_generator.outputs.OutputStrategy;
import java.util.ArrayList;
import java.util.List;

public class BloodSaturationDataGeneratorTest {

    private static final int PATIENTS = 10;
    private BloodSaturationDataGenerator satGen;

    @BeforeEach
    public void setup() {
        satGen = new BloodSaturationDataGenerator(PATIENTS);
    }

    @Test
    @DisplayName("Constructor creates generator")
    public void constructorTest() {
        assertNotNull(satGen, "Should instantiate generator");
    }

    @Test
    @DisplayName("Generate produces saturation values")
    public void generateProducesSaturation() {
        RecordingOutputStrategy output = new RecordingOutputStrategy();
        int id = 5;
        satGen.generate(id, output);

        boolean found = false;
        for (OutputRecord rec : output.getRecords()) {
            if (rec.patientId == id && "Saturation".equals(rec.recordType)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should output saturation");
    }

    @Test
    @DisplayName("Saturation values are within physiological range")
    public void valueRangesTest() {
        RecordingOutputStrategy output = new RecordingOutputStrategy();
        for (int id = 1; id <= PATIENTS; id++) {
            satGen.generate(id, output);
        }
        for (OutputRecord rec : output.getRecords()) {
            if ("Saturation".equals(rec.recordType)) {
                StringBuilder sb = new StringBuilder(rec.value);
                sb.deleteCharAt(sb.length() - 1); // Remove '%'
                double v = Double.parseDouble(sb.toString());
                assertTrue(v >= 80 && v <= 100, "Saturation in range: " + v);
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