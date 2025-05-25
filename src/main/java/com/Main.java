package com;

import java.net.URI;
import java.util.Scanner;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;
import com.cardio_generator.outputs.WebSocketOutputStrategy;
import com.data_management.WebSocketClientReader;

/**
 * Main entry point for the Signal Project application.
 * Allows running different components based on command-line parameters.
 */
public class Main {

    /**
     * Dispatches execution to the appropriate component based on the first command-line argument.
     * If "DataStorage" is specified, runs the DataStorage component.
     * Otherwise, runs the HealthDataSimulator (default).
     *
     * @param args command-line arguments
     * @throws Exception if an error occurs during execution
     */
    public static void main(String[] args) throws Exception {
       /*  
        if (args.length > 0 && args[0].equals("DataStorage")) {
            System.out.println("Starting DataStorage component...");
            // Pass all arguments except the first to DataStorage
            String[] remainingArgs = new String[args.length - 1];
            System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);
            DataStorage.main(remainingArgs);
        } else {
            System.out.println("Starting HealthDataSimulator component...");
            // If the first argument is "HealthDataSimulator", skip it
            String[] simulatorArgs = args;
            if (args.length > 0 && args[0].equals("HealthDataSimulator")) {
                simulatorArgs = new String[args.length - 1];
                System.arraycopy(args, 1, simulatorArgs, 0, args.length - 1);
            }
            HealthDataSimulator.main(simulatorArgs);
        }

        */
        URI serverUri = new URI("ws://localhost:8887");
        WebSocketOutputStrategy server = new WebSocketOutputStrategy(8887);
        WebSocketClientReader client = new WebSocketClientReader(serverUri);
        client.connect(); // Open the WebSocket connection

        Scanner scanner = new Scanner(System.in);
        System.out.println("type exit to get out:");

        long currentTime = System.currentTimeMillis();
        server.output(4234, currentTime, "BP", "653");

        while (true) {
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                break;
            }
            client.send(input);
        }

        client.close();
    }

    /**
     * Prints usage information for the application.
     */
    public static void printHelp() {
        System.out.println("Usage: java -jar cardio_generator-1.0-SNAPSHOT.jar [component] [options]");
        System.out.println("Components:");
        System.out.println("  DataStorage          - Run the DataStorage component");
        System.out.println("  HealthDataSimulator  - Run the HealthDataSimulator (default)");
        System.out.println();
        System.out.println("For HealthDataSimulator options:");
        System.out.println("  --patient-count <n>  - Number of patients to simulate");
        System.out.println("  --output <type>      - Output strategy (console, file:path, websocket:port, tcp:port)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar cardio_generator-1.0-SNAPSHOT.jar");
        System.out.println("  java -jar cardio_generator-1.0-SNAPSHOT.jar DataStorage");
        System.out.println("  java -jar cardio_generator-1.0-SNAPSHOT.jar HealthDataSimulator --patient-count 100");
    }
}