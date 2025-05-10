package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

// Renamed class from fileOutputStrategy to FileOutputStrategy due to UpperCamelCase.
/**
 *patient data is sent to separate files according data type.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Renamed BaseDirectory to baseDirectory to due to lowerCamelCase .
    
    private String baseDirectory;

    // CHnaged from file_map to fileMap because lowerCamelCase .
    
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Applies the output file directory
     *
     * @param baseDirectory file directory
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * According to the label data is sent to the correct file.
     *
     * @param patientId  patient's ID
     * @param timestamp data generation noted time
     * @param label label for data type
     * @param data value of the data
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Altererd FilePath to filePath due to lowerCamelCase rule (Section 5.2.7)
        // Set the filePath variable
        String filePath = fileMap.computeIfAbsent(label,
                k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        // Wrapped line to improve readability (Section 4.5) and to.comply with 100-column limit (Section 4.4)
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                    patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}