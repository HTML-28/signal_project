package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads patient data from output files in a specified directory and loads it into DataStorage.
 */
public class FileDataReader implements DataReader {

    private final String outputDirectory;

    /**
     * Constructs a FileDataReader for the given output directory.
     *
     * @param outputDirectory the directory containing the output files
     */
    public FileDataReader(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Reads all files in the output directory and loads their data into the provided DataStorage.
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the directory or files
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path directoryPath = Paths.get(outputDirectory);

        // Ensure the directory exists and is valid
        if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            throw new IOException("Output directory does not exist or is not a directory: " + outputDirectory);
        }

        // Collect all regular files in the directory (recursively)
        List<File> files = Files.walk(directoryPath)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        // Process each file found
        for (File file : files) {
            processFile(file, dataStorage);
        }
    }

    /**
     * Processes a single output file and stores its data in the data storage.
     * Each line is expected to be in CSV format: patientId,measurementValue,recordType,timestamp
     *
     * @param file the file to process
     * @param dataStorage the storage where data will be stored
     * @throws IOException if an error occurs while reading the file
     */
    private void processFile(File file, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    // Parse the line and add the data to storage
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        int patientId = Integer.parseInt(parts[0].trim());
                        double measurementValue = Double.parseDouble(parts[1].trim());
                        String recordType = parts[2].trim();
                        long timestamp = Long.parseLong(parts[3].trim());

                        dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
                    } else {
                        System.err.println("Skipping malformed line: " + line);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                }
            }
        }
    }
}