package data_management;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.*;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.WebSocketClientReader;
import com.data_management.PatientRecord;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketClientReader.
 * Uses an embedded WebSocket server to simulate real-time data.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WebSocketClientReaderTest {

    private static final int PORT = 8090;
    private ServerDemo server;
    private static final String SERVER_URI = "ws://localhost:" + PORT;    
    private WebSocketClientReader client;
    private DataStorage storage;

    @BeforeAll
    void launchServer() throws Exception {
        server = new ServerDemo(PORT);
        server.start();
        Thread.sleep(500); // Ensure server is ready
    }

    @AfterAll
    void shutdownServer() throws Exception {
        server.stop(500);
    }

    @BeforeEach
    void init() {
        storage = storage.getInstance();
        storage.reset();
        client = new WebSocketClientReader(SERVER_URI);
    }

    @AfterEach
    void cleanup() {
        client.close();
    }

    @Test
    void CalculationsTest() throws Exception {
        client.readData(storage);
        String msg = "1,3671627,BloodPressure,100";
        server.broadcast(msg);
        Thread.sleep(300);

        Patient patient = findPatient(1);
        assertNotNull(patient, "Patient should exist");
        assertEquals(1, patient.getRecords(0, Long.MAX_VALUE).size());
        PatientRecord rec = patient.getRecords(0, Long.MAX_VALUE).get(0);
        assertEquals(100, rec.getMeasurementValue(), 0.001);
        assertEquals("BloodPressure", rec.getRecordType());
        assertEquals(3671627L, rec.getTimestamp());
    }

    @Test
    void testConnection() throws IOException {
        // Should connect to the test server
        client.readData(storage);
        assertTrue(client.isConnected(), "Client should connect to server");
    }

    @Test
    void badMessageTest() throws Exception {
        client.readData(storage);
        server.broadcast("1,478172837,HeartRate,94");
        server.broadcast("malformed_data");
        Thread.sleep(200);

        Patient patient = findPatient(1);
        assertNotNull(patient, "Patient should be in the storage even after invalid message");
        assertEquals(1, patient.getRecords(0, Long.MAX_VALUE).size());
    }

    @Test
    void severalMessagesTest() throws Exception {
        client.readData(storage);
        server.broadcast("1,127378271,HeartRate,100");
        server.broadcast("1,1621234567891,Temperature,45");
        server.broadcast("2,1621234567892,BloodPressure,90");
        Thread.sleep(600);

        Patient p1 = findPatient(1);
        Patient p2 = findPatient(2);
        assertNotNull(p1, "Patient 1 should exist");
        assertNotNull(p2, "Patient 2 should exist");
        assertEquals(2, p1.getRecords(0, Long.MAX_VALUE).size());
        assertEquals(1, p2.getRecords(0, Long.MAX_VALUE).size());
    }



    @Test
    void ConnectionDownTest() {
        // Attempt connection to invalid port
        WebSocketClientReader badClient = new WebSocketClientReader("ws://localhost:1234", 2);
        IOException ex = assertThrows(IOException.class, () -> badClient.readData(storage));
        assertTrue(ex.getMessage().contains("Failed to connect"));
    }

    // Helper: Find patient by ID
    private Patient findPatient(int id) {
        return storage.getAllPatients().stream()
                .filter(p -> p.getPatientId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Minimal WebSocket server for test purposes.
     */
    private static class ServerDemo extends WebSocketServer {
        ServerDemo(int port) {
            super(new InetSocketAddress(port));
        }
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            // Connection established
        }
        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            // Connection closed
        }
        @Override
        public void onMessage(WebSocket conn, String message) {
            // No-op for test
        }
        @Override
        public void onError(WebSocket conn, Exception ex) {
            System.err.println("Server error: " + ex.getMessage());
        }
        @Override
        public void onStart() {
            // Server started
        }
    }
}
