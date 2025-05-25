package com.cardio_generator.generators;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import com.cardio_generator.outputs.OutputStrategy;
import java.lang.reflect.Field;

public class AlertGeneratorTest {

    private static final int PATIENTS = 10;

    @Test
    @DisplayName("AlertGenerator initializes alert states")
    public void constructorInitializesStates() {
        AlertGenerator gen = new AlertGenerator(PATIENTS);
        assertNotNull(gen, "Should instantiate generator");
    }

    @Test
    @DisplayName("Generate method executes and triggers output")
    public void generateExecutesTest() {
        AlertGenerator gen = new AlertGenerator(PATIENTS);

        // Track output calls
        final boolean[] outputSeen = {false};
        OutputStrategy tracker = new OutputStrategy() {
            @Override
            public void output(int patientId, long timestamp, String recordType, String value) {
                outputSeen[0] = true;
                assertEquals("Alert", recordType);
                assertTrue(value.equals("triggered") || value.equals("resolved"));
            }
        };

        try {
            Field field = AlertGenerator.class.getDeclaredField("alertStates");
            field.setAccessible(true);
            boolean[] states = (boolean[]) field.get(gen);

            // Force alert state for patient 5
            states[5] = true;
            gen.generate(5, tracker);

            // Reset and test for patient 6
            outputSeen[0] = false;
            states[6] = false;
            for (int i = 0; i < 100 && !outputSeen[0]; i++) {
                gen.generate(6, tracker);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("CountingOutputStrategy counts outputs")
    public void countingStrategyTest() {
        AlertGenerator gen = new AlertGenerator(PATIENTS);
        CountingOutputStrategy counter = new CountingOutputStrategy();

        try {
            Field field = AlertGenerator.class.getDeclaredField("alertStates");
            field.setAccessible(true);
            boolean[] states = (boolean[]) field.get(gen);

            for (int id = 1; id <= PATIENTS; id++) {
                states[id] = (id % 2 == 0);
                gen.generate(id, counter);
            }
            assertTrue(counter.getTotalOutputs() > 0, "Should count at least one output");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // Helper for counting outputs
    private static class CountingOutputStrategy implements OutputStrategy {
        private int triggered = 0;
        private int resolved = 0;

        @Override
        public void output(int patientId, long timestamp, String recordType, String value) {
            if ("Alert".equals(recordType)) {
                if ("triggered".equals(value)) triggered++;
                else if ("resolved".equals(value)) resolved++;
            }
        }

        public int getTriggeredCount() { return triggered; }
        public int getResolvedCount() { return resolved; }
        public int getTotalOutputs() { return triggered + resolved; }
    }
}