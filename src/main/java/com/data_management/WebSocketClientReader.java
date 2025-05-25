package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClientReader extends WebSocketClient implements DataReader {
   private DataStorage storage;

   // ws://host:port/path is the format for WebSocket URIs
    public WebSocketClientReader(URI serverUri) {
        super(serverUri);
    }

  @Override
    public void start(DataStorage storage) throws IOException {
        this.storage = storage;
            connect(); // open socket
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
    // not used here
    }


    @Override
    public void stop() throws IOException {
    close();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("WebSocket opened");
    }
    
    @Override
    public void onError(Exception e) {
        System.out.println("WebSocket error" + e.getMessage());
    }

    @Override
    public void onMessage(String s) {
        System.out.println(s);
        String[] components = s.split(",");
        if (components.length < 4) return;

        try {
            int patientID = Integer.parseInt(components[0]);
            long timeStampp = Long.parseLong(components[1]);
            String label = components[2];
            String values = components[3];
            if (values.endsWith("%")) {
                values = values.substring(0, values.length() - 1);
            }
            double measurementVal = Double.parseDouble(values);

            // store
            storage.addPatientData(patientID, measurementVal, label, timeStampp);
        } catch (Exception e) {
            System.out.println("Error parsing data: " + s);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("WebSocket closed");
    }


}
