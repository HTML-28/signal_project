package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

//class must match file name, also it has to be capitalized
public class FileOutputStrategy implements OutputStrategy {

    // variable names start with lowercase
    private String baseDirectory;

    // final variables are capitalized and static
    public static final ConcurrentHashMap<String, String> FILE_MAP = new ConcurrentHashMap<>();

    // constructor must match class name, case sensitive
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    // changed to camelCase
    @Override
    public void output(int patientId, long timeStamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }

        // changed FilePath to filePath :)
        // Set the FilePath variable
        String filePath = FILE_MAP.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timeStamp, label, data);
        } catch (Exception IOException) { // error catch has to be more specific
            System.err.println("Error writing to file " + filePath + ": " + IOException.getMessage());
        }
    }
}