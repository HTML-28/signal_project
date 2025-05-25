package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

/**
 * Reads patient data in real-time from a WebSocket server.
 * Implements DataReader for continuous data ingestion.
 */
public class WebSocketClientReader implements DataReader {

    private final String SERVER_URI;
    private final int endTime;
    private WebSocketClient client;
    private volatile boolean isConnected = false;
    private DataStorage dataStorage;

    /**
     * Create a WebSocketClientReader with default timeout.
     * @param SERVER_URI WebSocket server URI (e.g., ws://localhost:8080)
     */
    public WebSocketClientReader(String SERVER_URI) {
        this(SERVER_URI, 10);
    }

    /**
     * Create a WebSocketClientReader with custom timeout.
     * @param SERVER_URI WebSocket server URI
     * @param endTime Timeout in seconds
     */
    public WebSocketClientReader(String SERVER_URI, int endTime) {
        this.SERVER_URI = SERVER_URI;
        this.endTime = endTime;
    }

    /**
     * Connects to the WebSocket server and starts listening for messages.
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            client = clientCreation(new URI(SERVER_URI), latch);
            client.connect();

            // Wait for connection or timeout
            if (!latch.await(endTime, TimeUnit.SECONDS)) {
                throw new IOException("Failed to connect to WebSocket server at " + SERVER_URI);
            }
            isConnected = true;
        } catch (URISyntaxException e) {
            throw new IOException("Invalid WebSocket URI: " + SERVER_URI, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while connecting to WebSocket server", e);
        }
    }

    /**
     * Closes the WebSocket connection if open.
     */
    public void close() {
        if (client != null && isConnected) {
            client.close();
            isConnected = false;
        }
    }

    /**
     * Builds a WebSocketClient with handlers for connection and message events.
     */
    private WebSocketClient clientCreation(URI uri, CountDownLatch latch) {
        return new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("Connected to WebSocket: " + uri);
                latch.countDown();
            }

            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("WebSocket closed: " + reason + " (code: " + code + ")");
                isConnected = false;
                // Attempt reconnect if closed by server
                if (remote && dataStorage != null) {
                    System.out.println("Trying to reconnect...");
                    try {
                        reconnect();
                    } catch (Exception e) {
                        System.err.println("Reconnect failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("WebSocket error: " + ex.getMessage());
            }
        };
    }

    /**
     * Parses and stores a message from the server.
     * Expected: patientId,timestamp,recordType,value
     */
    private void handleMessage(String message) {
        if (message == null || message.isEmpty()) return;
        String[] tokens = message.split(",");
        if (tokens.length < 4) {
            System.err.println("Malformed message: " + message);
            return;
        }
        try {
            int patientId = Integer.parseInt(tokens[0].trim());
            long timestamp = Long.parseLong(tokens[1].trim());
            String recordType = tokens[2].trim();
            double value = Double.parseDouble(tokens[3].trim());
            dataStorage.addPatientData(patientId, value, recordType, timestamp);
        } catch (NumberFormatException e) {
            System.err.println("Parse error in message: " + message + " - " + e.getMessage());
        }
    }

    /**
     * Returns true if the client is connected.
     */
    public boolean isConnected() {
        return isConnected;
    }

}
