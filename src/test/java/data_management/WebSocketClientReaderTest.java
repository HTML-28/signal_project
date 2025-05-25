package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketClientReader.
 * Verifies correct parsing and storage of incoming messages.
 */
class WebSocketClientReaderTest {

    /**
     * Subclass to override connect() for testing purposes.
     * Prevents actual network operations.
     */
    private static class DummyReader extends WebSocketClientReader {
        public DummyReader(URI uri) {
            super(uri);
        }
        @Override
        public void connect() {
            // Intentionally left blank to avoid real connections
        }
    }

    private DataStorage dataRepo;
    private WebSocketClientReader wsReader;

    /**
     * Prepare a fresh DataStorage and a test WebSocketClientReader before each test.
     */
    @BeforeEach
    void initialize() throws IOException {
        dataRepo = DataStorage.getInstance();
        dataRepo.reset(); // Clear any previous test data
        wsReader = new DummyReader(URI.create("ws://localhost:9999"));
        wsReader.start(dataRepo); // Assign storage to reader
    }

    /**
     * Test that a well-formed message is parsed and stored as a single PatientRecord.
     */
    @Test
    void validMessageShouldStoreRecord() {
        wsReader.onMessage("5,1612345678000,HeartRate,72.0");

        List<PatientRecord> found = dataRepo.getRecords(
                5, 1612345678000L, 1612345678000L
        );
        assertEquals(1, found.size(), "Expected one record to be stored");

        PatientRecord rec = found.get(0);
        assertEquals(5, rec.getPatientId(), "Patient ID mismatch");
        assertEquals("HeartRate", rec.getRecordType(), "Record type mismatch");
        assertEquals(72.0, rec.getMeasurementValue(), 0.0001, "Measurement value mismatch");
        assertEquals(1612345678000L, rec.getTimestamp(), "Timestamp mismatch");
    }

    /**
     * Test that a message with a percent sign in the value is handled correctly.
     */
    @Test
    void percentSignShouldBeStrippedFromValue() {
        wsReader.onMessage("2,1000,Temp,37.5%");

        List<PatientRecord> found = dataRepo.getRecords(2, 1000L, 1000L);
        assertEquals(1, found.size(), "Percent value should still be stored");
        assertEquals(37.5, found.get(0).getMeasurementValue(), 0.0001,
                "Percent sign should be removed from value");
    }

    /**
     * Test that a message with too few fields is ignored.
     */
    @Test
    void incompleteMessageShouldBeIgnored() {
        wsReader.onMessage("1,2000,OnlyThree");
        List<PatientRecord> found = dataRepo.getRecords(1, 0, Long.MAX_VALUE);
        assertTrue(found.isEmpty(), "Malformed message should not be stored");
    }

    /**
     * Test that a message with an invalid number format is safely ignored.
     */
    @Test
    void invalidNumberFormatShouldNotStore() {
        wsReader.onMessage("x,2000,Label,50.0");
        List<PatientRecord> found = dataRepo.getAllPatients().isEmpty()
                ? List.of()
                : dataRepo.getRecords(0, 0, Long.MAX_VALUE);
        assertTrue(found.isEmpty(), "Parsing errors should not result in storage");
    }

    /**
     * Test that multiple valid messages for the same patient accumulate records.
     */
    @Test
    void multipleValidMessagesShouldAccumulate() {
        wsReader.onMessage("3,500,ECG,10");
        wsReader.onMessage("3,600,ECG,12");

        List<PatientRecord> found = dataRepo.getRecords(3, 0, 1000);
        assertEquals(2, found.size(), "Both records should be present");
    }
}
