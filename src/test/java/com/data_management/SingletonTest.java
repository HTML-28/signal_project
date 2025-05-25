package com.data_management;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

import com.cardio_generator.HealthDataSimulator;

public class SingletonTest {

    @Test
    @DisplayName("DataStorage returns singleton instance")
    public void dataStorageSingletonTest() {
        DataStorage s1 = DataStorage.getInstance();
        DataStorage s2 = DataStorage.getInstance();
        assertSame(s1, s2, "Should be same DataStorage instance");
    }

    @Test
    @DisplayName("HealthDataSimulator returns singleton instance")
    public void healthDataSimulatorSingletonTest() {
        HealthDataSimulator h1 = HealthDataSimulator.getInstance();
        HealthDataSimulator h2 = HealthDataSimulator.getInstance();
        assertSame(h1, h2, "Should be same HealthDataSimulator instance");
    }

    @Test
    @DisplayName("DataStorage singleton is thread-safe")
    public void dataStorageConcurrentSingletonTest() throws Exception {
        final int threads = 10;
        final Set<DataStorage> seen = Collections.synchronizedSet(new HashSet<>());
        final CountDownLatch latch = new CountDownLatch(threads);

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    DataStorage inst = DataStorage.getInstance();
                    seen.add(inst);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS), "Threads should finish");
        assertEquals(1, seen.size(), "All threads should see same instance");
    }

    @Test
    @DisplayName("DataStorage reset clears data")
    public void dataStorageResetTest() {
        DataStorage ds = DataStorage.getInstance();
        ds.addPatientData(1, 100.0, "TestData", System.currentTimeMillis());
        assertFalse(ds.getRecords(1, 0, Long.MAX_VALUE).isEmpty(), "Should have data before reset");
        ds.reset();
        assertTrue(ds.getRecords(1, 0, Long.MAX_VALUE).isEmpty(), "Should be empty after reset");
    }

    @Test
    @DisplayName("DataStorage clearAllData removes all records")
    public void dataStorageClearAllTest() {
        DataStorage ds = DataStorage.getInstance();
        ds.addPatientData(1, 100.0, "TestData", System.currentTimeMillis());
        assertFalse(ds.getRecords(1, 0, Long.MAX_VALUE).isEmpty(), "Should have data before clear");
        ds.clearAllData();
        assertTrue(ds.getRecords(1, 0, Long.MAX_VALUE).isEmpty(), "Should be empty after clear");
    }
}