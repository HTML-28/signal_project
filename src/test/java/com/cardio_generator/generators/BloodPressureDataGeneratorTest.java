package com.cardio_generator.generators;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import com.cardio_generator.outputs.OutputStrategy;
import java.util.ArrayList;
import java.util.List;

public class BloodPressureDataGeneratorTest {

    private static final int PATIENTS = 10;
    private BloodPressureDataGenerator bpGen;

    @BeforeEach
    public void prepare() {
        bpGen = new BloodPressureDataGenerator(PATIENTS);
    }

    @Test
    @DisplayName("Constructor creates generator")
    public void constructorTest() {
        assertNotNull(bpGen, "Should instantiate generator");
    }

    @Test
    @DisplayName("Generate produces systolic and diastolic values")
    public void generateProducesValues() {
        RecordingOutputStrategy output = new RecordingOutputStrategy();
        int id = 5;
        bpGen.generate(id, output);

        boolean foundSys = false, foundDia = false;
        for (OutputRecord rec : output.getRecords()) {
            if (rec.patientId == id) {
                if ("SystolicPressure".equals(rec.recordType)) foundSys = true;
                else if ("DiastolicPressure".equals(rec.recordType)) foundDia = true;
            }
        }
        assertTrue(foundSys, "Should output systolic");
        assertTrue(foundDia, "Should output diastolic");
    }

    @Test
    @DisplayName("Values are within physiological ranges")
    public void valueRangesTest() {
        RecordingOutputStrategy output = new RecordingOutputStrategy();
        for (int id = 1; id <= PATIENTS; id++) {
            bpGen.generate(id, output);
        }
        for (OutputRecord rec : output.getRecords()) {
            if ("SystolicPressure".equals(rec.recordType)) {
                double v = Double.parseDouble(rec.value);
                assertTrue(v >= 80 && v <= 200, "Systolic in range: " + v);
            } else if ("DiastolicPressure".equals(rec.recordType)) {
                double v = Double.parseDouble(rec.value);
                assertTrue(v >= 40 && v <= 120, "Diastolic in range: " + v);
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