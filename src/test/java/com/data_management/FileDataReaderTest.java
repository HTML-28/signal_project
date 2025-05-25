package com.data_management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileDataReader.
 * Each test creates its own scenario and verifies FileDataReader's behavior.
 */
class FileDataReaderTest {

    @TempDir
    Path tempDir; // JUnit provides a temporary directory for file operations

    @Test
    void testReadingFile() throws IOException {
        // Arrange: Write a file with valid patient data
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(testFile))) {
            bw.write("101,120.0,SystolicBP,1000\n");
            bw.write("101,80.0,DiastolicBP,1000\n");
        }

        DataStorage storage = DataStorage.getInstance();
        FileDataReader reader = new FileDataReader(tempDir.toString());

        // Act: Read the data from the file
        reader.readData(storage);

        // Assert: No explicit assertions, but should not throw and should parse lines
        // (Could be improved by checking storage contents)
    }

    @Test
    void testEmptyDirectory() throws IOException {
        // Arrange: Directory is empty, no files to read
        FileDataReader reader = new FileDataReader(tempDir.toString());
        DataStorage storage = DataStorage.getInstance();

        // Act & Assert: Should not throw any exceptions
        reader.readData(storage);
    }

    @Test
    void testBadData() throws IOException {
        // Arrange: Write a file with invalid data format
        File badFile = tempDir.resolve("bad.txt").toFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(badFile))) {
            bw.write("not a valid line\n");
        }

        DataStorage storage = DataStorage.getInstance();
        FileDataReader reader = new FileDataReader(tempDir.toString());

        // Act & Assert: Should skip bad lines and not throw
        reader.readData(storage);
    }

    @Test
    void testDirectoryNotFound() {
        // Arrange: Use a directory path that does not exist
        String nonExistentDir = "C:/this/does/not/exist";
        FileDataReader reader = new FileDataReader(nonExistentDir);
        DataStorage storage = DataStorage.getInstance();

        // Act & Assert: Should throw IOException due to missing directory
        Exception exception = assertThrows(IOException.class, () -> {
            reader.readData(storage);
        });
        // Optionally, check exception message
        assertTrue(exception.getMessage().contains("does not exist"));
    }
}