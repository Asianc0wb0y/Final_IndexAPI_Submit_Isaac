package de.solactive.challenge.indexapi.services;

import de.solactive.challenge.indexapi.dto.ShareAdditionDTO;
import de.solactive.challenge.indexapi.dto.ShareDeletionDTO;
import de.solactive.challenge.indexapi.dto.ShareDividendDTO;
import de.solactive.challenge.indexapi.entities.IndexEntity;
import de.solactive.challenge.indexapi.entities.ShareEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class IndexServiceTest {

    private IndexService indexService;


    @BeforeEach
    void setUp() {
        // Initialize the service with a null mapper (mock if needed later)
        indexService = new IndexService(null);

        // Create individual share entities
        ShareEntity appleShare = new ShareEntity("AAPL.OQ", 150.0, 10.0);
        ShareEntity metaShare = new ShareEntity("META.OQ", 200.0, 5.0);
        ShareEntity intlShare = new ShareEntity("INTL.OQ", 90.0, 6.0);

        // Add shares to a map
        Map<String, ShareEntity> sharesMap = new ConcurrentHashMap<>();
        sharesMap.put("AAPL.OQ", appleShare);
        sharesMap.put("META.OQ", metaShare);
        sharesMap.put("INTL.OQ", intlShare);

        // Create the index entity with the shares map
        IndexEntity indexEntity = new IndexEntity("INDEX_1", sharesMap);

        // Add the index entity to the index map in the service
        indexService.getIndexMap().put("INDEX_1", indexEntity);
    }


    @Test
    void testAddShareToIndex_Success() {
        ShareAdditionDTO dto = new ShareAdditionDTO("IBM.OQ", 100.0, 20.0, "INDEX_1");

        boolean result = indexService.addShareToIndex(dto);

        assertTrue(result);
        assertNotNull(indexService.getIndexMap().get("INDEX_1").getShares().get("IBM.OQ"));
    }

    @Test
    void testAddShareToIndex_IndexNotFound() {
        ShareAdditionDTO dto = new ShareAdditionDTO("IBM.OQ", 100.0, 20.0, "NON_EXISTENT_INDEX");

        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            indexService.addShareToIndex(dto);
        });

        assertEquals("Index does not exist", exception.getMessage());
    }

    @Test
    void testAddShareToIndex_ShareAlreadyExists() {
        ShareAdditionDTO dto = new ShareAdditionDTO("AAPL.OQ", 150.0, 10.0, "INDEX_1");

        boolean result = indexService.addShareToIndex(dto);

        assertFalse(result);  // Share already exists
    }

    @Test
    void testDeleteShareFromIndex_Success() {
        ShareDeletionDTO dto = new ShareDeletionDTO("AAPL.OQ", "INDEX_1");
        indexService.deleteShareFromIndex(dto);


        assertNull(indexService.getIndexMap().get("INDEX_1").getShares().get("AAPL.OQ"));
        assertEquals(2, indexService.getIndexMap().get("INDEX_1").getShares().size());
        //2 because the setUp() creates 3
    }

    @Test
    void testDeleteShareFromIndex_IndexNotFound() {
        ShareDeletionDTO dto = new ShareDeletionDTO("AAPL.OQ", "NON_EXISTENT_INDEX");

        //Make sure there are 2 members left


        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            indexService.deleteShareFromIndex(dto);
        });

        assertEquals("Index not found: NON_EXISTENT_INDEX", exception.getMessage());
    }

    @Test
    void testDeleteShareFromIndex_IndexMustHaveAtLeast3Members() {
        // Set up an index with exactly 3 members
        Map<String, ShareEntity> shares = new ConcurrentHashMap<>(Map.of(
                "AAPL.OQ", new ShareEntity("AAPL.OQ", 150.0, 10.0),
                "AMDD.OQ", new ShareEntity("AMDD.OQ", 200.0, 5.0),
                "ORAC.OQ", new ShareEntity("ORAC.OQ", 200.0, 5.0)));

        IndexEntity indexEntity = new IndexEntity("INDEX_3Members",shares);
        indexService.getIndexMap().put("INDEX_3Members", indexEntity);


        // Delete one member
        ShareDeletionDTO dto = new ShareDeletionDTO("AAPL.OQ", "INDEX_3Members");
        indexService.deleteShareFromIndex(dto);

        // Try deleting the second member and expect an exception
        ShareDeletionDTO secondDeletion = new ShareDeletionDTO("AMDD.OQ", "INDEX_3Members");
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            indexService.deleteShareFromIndex(secondDeletion);
        });
        assertEquals("Index must have at least 3 members before deletion", exception.getMessage());
    }

    @Test
    void testDeleteShareFromIndex_ShareNotFound() {
        ShareDeletionDTO dto = new ShareDeletionDTO("TSLA.OQ", "INDEX_1");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            indexService.deleteShareFromIndex(dto);
        });

        assertEquals("Share not found in the index: TSLA.OQ", exception.getMessage());
    }


    @Test
    void testApplyDividend_Success() {
        ShareDividendDTO dto = new ShareDividendDTO("AAPL.OQ", 5.0);

        indexService.applyDividend(dto);

        ShareEntity share = indexService.getIndexMap().get("INDEX_1").getShares().get("AAPL.OQ");
        assertEquals(145.0, share.getSharePrice(), 0.01);
    }

    @Test
    void testApplyDividend_DividendMustBePositive() {
        ShareDividendDTO dto = new ShareDividendDTO("AAPL.OQ", -10.0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            indexService.applyDividend(dto);
        });

        assertEquals("Dividend cannot be negative", exception.getMessage());
    }

    @Test
    void testApplyDividend_DividendGreaterThanSharePrice() {
        ShareDividendDTO dto = new ShareDividendDTO("AAPL.OQ", 151.0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            indexService.applyDividend(dto);
        });

        assertEquals("Dividend cannot greater than current share price of AAPL.OQ", exception.getMessage());
    }

    @Test
    void testApplyDividend_ShareNotFound() {
        ShareDividendDTO dto = new ShareDividendDTO("TSLA.OQ", 7.0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            indexService.applyDividend(dto);
        });

        assertEquals("Share not found in any index: TSLA.OQ", exception.getMessage());
    }
}
