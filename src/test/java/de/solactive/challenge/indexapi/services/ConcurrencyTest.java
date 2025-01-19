
package de.solactive.challenge.indexapi.services;

import de.solactive.challenge.indexapi.dto.*;
import de.solactive.challenge.indexapi.entities.*;
import de.solactive.challenge.indexapi.mappers.IndexMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConcurrencyTest {

    private IndexService indexService;

    @BeforeEach
    void setUp() {
        indexService = new IndexService(new IndexMapperImpl());

        // Create 2 indices
        indexService.createIndex(new IndexDTO("INDEX_1", List.of(
                new ShareDTO("A.OQ", 100.0, 10.0),
                new ShareDTO("B.OQ", 200.0, 20.0)
        )));
        indexService.createIndex(new IndexDTO("INDEX_2", List.of(
                new ShareDTO("C.OQ", 150.0, 15.0),
                new ShareDTO("D.OQ", 250.0, 25.0)
        )));
    }

    @Test
    void testConcurrentOperations() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10); // Use 10 threads

        // Task 1: Apply dividends concurrently
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                ShareDividendDTO dividendDTO = new ShareDividendDTO("A.OQ", 10.0);
                indexService.applyDividend(dividendDTO);
            });
        }

        // Task 2: Add shares concurrently
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            executor.submit(() -> {
                ShareAdditionDTO additionDTO = new ShareAdditionDTO("THREAD_SHARE_" + threadId, 50.0, 5.0, "INDEX_1");
                indexService.addShareToIndex(additionDTO);
            });
        }

        // Task 3: Create indices concurrently
        for (int i = 0; i < 3; i++) {
            final int indexId = i + 3; // Create INDEX_3, INDEX_4, INDEX_5
            executor.submit(() -> {
                indexService.createIndex(new IndexDTO("INDEX_" + indexId, List.of(
                        new ShareDTO("E.OQ", 300.0, 30.0),
                        new ShareDTO("F.OQ", 350.0, 35.0)
                )));
            });
        }

        // Task 4: Delete shares concurrently
        executor.submit(() -> {
            ShareDeletionDTO deletionDTO = new ShareDeletionDTO("B.OQ", "INDEX_1");
            indexService.deleteShareFromIndex(deletionDTO);
        });

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS); // Wait for threads to complete

        // Validate Results

        // 1. Check INDEX_1 after dividend and additions
        IndexEntity index1 = indexService.getIndexMap().get("INDEX_1");
        ShareEntity shareA = index1.getShares().get("A.OQ");
        assertEquals(50.0, shareA.getSharePrice(), 0.01); // $100 - ($10 * 5 dividends)
        assertEquals(6, index1.getShares().size()); // 6 shares

        // 2. Check created indices
        assertEquals(5, indexService.getIndexMap().size()); // Original 2 + 3 created

        // 3. Ensure INDEX_1 no longer contains B.OQ
        assertNull(index1.getShares().get("B.OQ"));
    }
}
