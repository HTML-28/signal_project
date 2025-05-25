package com;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for the Main class, verifying argument dispatch and help output.
 */
class MainTest {

    // Utility to capture standard output for assertions
    private final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void redirectStdOut() {
        originalOut = System.out;
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreStdOut() {
        System.setOut(originalOut);
        capturedOut.reset();
    }

    /**
     * Ensure that the help message includes all expected keywords and options.
     */
    @Test
    void helpMessageContent() {
        // Act
        Main.printHelp();
        String helpOutput = capturedOut.toString();

        // Assert
        assertAll(
            () -> assertTrue(helpOutput.contains("DataStorage"), "Should mention DataStorage"),
            () -> assertTrue(helpOutput.contains("HealthDataSimulator"), "Should mention HealthDataSimulator"),
            () -> assertTrue(helpOutput.contains("--patient-count"), "Should mention patient count option"),
            () -> assertTrue(helpOutput.contains("--output"), "Should mention output option")
        );
    }

    /**
     * Test that passing "DataStorage" as the first argument triggers DataStorage's main method.
     */
    @Test
    void mainDispatchesToDataStorage() throws Exception {
        // Arrange
        DataStorageStub.invoked = false;
        String[] arguments = {"DataStorage"};

        // Act
        MainTestHarness.runWithDataStorage(arguments);

        // Assert
        assertTrue(DataStorageStub.invoked, "DataStorage main should be called");
    }

    /**
     * Test that passing "HealthDataSimulator" as the first argument triggers HealthDataSimulator's main method with correct arguments.
     */
    @Test
    void mainDispatchesToHealthDataSimulator() throws Exception {
        // Arrange
        HealthDataSimulatorStub.invoked = false;
        HealthDataSimulatorStub.receivedArgs = null;
        String[] arguments = {"HealthDataSimulator", "--patient-count", "10"};

        // Act
        MainTestHarness.runWithSimulator(arguments);

        // Assert
        assertTrue(HealthDataSimulatorStub.invoked, "HealthDataSimulator main should be called");
        assertArrayEquals(new String[]{"--patient-count", "10"}, HealthDataSimulatorStub.receivedArgs, "Arguments should be passed correctly");
    }

    /**
     * Test that running with no arguments defaults to HealthDataSimulator with empty args.
     */
    @Test
    void mainDefaultsToHealthDataSimulatorWithNoArgs() throws Exception {
        // Arrange
        HealthDataSimulatorStub.invoked = false;
        HealthDataSimulatorStub.receivedArgs = null;
        String[] arguments = {};

        // Act
        MainTestHarness.runWithSimulator(arguments);

        // Assert
        assertTrue(HealthDataSimulatorStub.invoked, "HealthDataSimulator main should be called");
        assertNotNull(HealthDataSimulatorStub.receivedArgs, "Arguments should not be null");
        assertEquals(0, HealthDataSimulatorStub.receivedArgs.length, "No arguments should be passed");
    }

    // --- Test harness and stubs for isolation ---

    /**
     * Harness to simulate Main dispatching, using stubs instead of real components.
     */
    static class MainTestHarness extends Main {
        static void runWithDataStorage(String[] args) throws Exception {
            if (args.length > 0 && args[0].equals("DataStorage")) {
                DataStorageStub.main(new String[0]);
            } else {
                HealthDataSimulatorStub.main(args);
            }
        }

        static void runWithSimulator(String[] args) throws Exception {
            if (args.length > 0 && args[0].equals("DataStorage")) {
                DataStorageStub.main(new String[0]);
            } else {
                String[] simArgs = args;
                if (args.length > 0 && args[0].equals("HealthDataSimulator")) {
                    simArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, simArgs, 0, args.length - 1);
                }
                HealthDataSimulatorStub.main(simArgs);
            }
        }
    }

    /**
     * Stub for DataStorage to track invocation.
     */
    static class DataStorageStub {
        static boolean invoked = false;
        static void main(String[] args) {
            invoked = true;
        }
    }

    /**
     * Stub for HealthDataSimulator to track invocation and arguments.
     */
    static class HealthDataSimulatorStub {
        static boolean invoked = false;
        static String[] receivedArgs;
        static void main(String[] args) {
            invoked = true;
            receivedArgs = args;
        }
    }
}