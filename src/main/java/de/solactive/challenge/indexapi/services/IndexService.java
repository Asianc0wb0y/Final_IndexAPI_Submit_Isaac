package de.solactive.challenge.indexapi.services;


import de.solactive.challenge.indexapi.dto.*;
import de.solactive.challenge.indexapi.entities.*;
import de.solactive.challenge.indexapi.mappers.IndexMapper;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class IndexService {

    private final Map<String, IndexEntity> indexMap = new ConcurrentHashMap<>();
    private final Map<String,ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private final IndexMapper indexMapper;

    public IndexService(IndexMapper indexMapper) {
        this.indexMapper = indexMapper;
    }

    // for IndexServiceTest only
    public Map<String, IndexEntity> getIndexMap() {
        return indexMap;
    }

    // Create Index
    public boolean createIndex(IndexDTO indexDTO) {

        String indexName = indexDTO.getIndexName();

        // Locking for Index Creation
        ReentrantLock lock = lockMap.computeIfAbsent(indexName, k -> new ReentrantLock());
        lock.lock();
        try{
            if (indexMap.containsKey(indexName)) {
            return false; // Index already exists
        }

        IndexEntity indexEntity = indexMapper.toEntity(indexDTO);
        indexMap.put(indexName, indexEntity);
        return true;
    } finally {
            lock.unlock(); // Making sure no persistant lock in case something goes wrong
        }
    }

    public boolean addShareToIndex(ShareAdditionDTO shareAdditionDTO) {

        // Locking for Share Addition
        ReentrantLock lock = lockMap.computeIfAbsent(shareAdditionDTO.getIndexName(), k -> new ReentrantLock());
        lock.lock();
        try {
            IndexEntity indexEntity = indexMap.get(shareAdditionDTO.getIndexName());
            // validation
            if (indexEntity == null) {
                throw new NoSuchElementException("Index does not exist"); //404
            }
            if (indexEntity.getShares().containsKey(shareAdditionDTO.getShareName())) {
                //throw new IllegalArgumentException("Share already exist");
                return false;
            }

          /* Logic to add Share
            1. Total Index Value = Get Current Index value + New Share Price * new number of shares
            2. The number of share of each share is then readjusted as
                NumOfShare * Current Index Value / Total Index Value
         */

            double currentIndexValue = calculateTotalIndexValue(indexEntity);
            double extraShareValue = shareAdditionDTO.getSharePrice() * shareAdditionDTO.getNumberOfShares();

            indexEntity.getShares().put(shareAdditionDTO.getShareName(),
                    new ShareEntity(shareAdditionDTO.getShareName(), shareAdditionDTO.getSharePrice(), shareAdditionDTO.getNumberOfShares()));

            for (ShareEntity shareEntity : indexEntity.getShares().values()) {
                shareEntity.setNumberOfShares(
                        shareEntity.getNumberOfShares() * currentIndexValue / (currentIndexValue + extraShareValue));
            }
            // Share added successfully
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void deleteShareFromIndex(ShareDeletionDTO shareDeletionDTO) {

        // Locking for Share Deletion
        ReentrantLock lock = lockMap.computeIfAbsent(shareDeletionDTO.getIndexName(), k -> new ReentrantLock());
        lock.lock();
        try {

            IndexEntity indexEntity = indexMap.get(shareDeletionDTO.getIndexName());
            if (indexEntity == null) {
                throw new NoSuchElementException("Index not found: " + shareDeletionDTO.getIndexName());
            }

            if (indexEntity.getShares().size() < 3) {
                throw new IllegalStateException("Index must have at least 3 members before deletion");
            }

            ShareEntity shareToDelete = indexEntity.getShares().get(shareDeletionDTO.getShareName());

            if (shareToDelete == null) {
                throw new IllegalArgumentException("Share not found in the index: " + shareDeletionDTO.getShareName());
            }

            double currentIndexValue = calculateTotalIndexValue(indexEntity);
            double removedShareValue = shareToDelete.getSharePrice() * shareToDelete.getNumberOfShares();

            indexEntity.getShares().remove(shareToDelete.getShareName());

            // Adjust remaining shares proportionally to maintain the index value
            for (ShareEntity share : indexEntity.getShares().values()) {

                share.setNumberOfShares(
                        share.getNumberOfShares() * currentIndexValue / (currentIndexValue - removedShareValue));
            }
        } finally {
            lock.unlock();
        }

    }
    public void applyDividend(ShareDividendDTO shareDividendDTO) {

        double curDividend = shareDividendDTO.getDividend();
       // Dividend Validation (1)
        if (curDividend < 0) {
            throw new IllegalArgumentException("Dividend cannot be negative");
        }

        // Locking for Dividend Adjustment after obvious validation above that doesn't need locking

        List<String> sortedIndexNames = indexMap.keySet().stream().sorted().toList();
        List<ReentrantLock> acquiredLocks = new ArrayList<>();

        try {
            // Locking in a sorted order to prevent deadlock from another thread with the same dividend operation
            for (String indexName : sortedIndexNames) {
                ReentrantLock lock = lockMap.computeIfAbsent(indexName, k -> new ReentrantLock());
                lock.lock();
                acquiredLocks.add(lock); // Track acquired locks for later release
            }

            // Dividend Operation
            boolean shareFound = false;

            for (IndexEntity indexEntity : indexMap.values()) {   // For each Index
                ShareEntity shareEntity = indexEntity.getShares().get(shareDividendDTO.getShareName()); // Share that needs readjustment due to price
                if (shareEntity != null) {
                    shareFound = true;
                    double currentIndexValue = calculateTotalIndexValue(indexEntity);
                    // This Index contains a member with this share
                    double curSharePrice = shareEntity.getSharePrice();
                    if (curDividend > curSharePrice) {
                        throw new IllegalArgumentException("Dividend cannot greater than current share price of " + shareDividendDTO.getShareName());
                    }
                    shareEntity.setSharePrice(curSharePrice - curDividend);

                    // Readjust the shares of this Index to maintain the same Index Value
                    double reductionInIndexValue = curDividend * shareEntity.getNumberOfShares();
                    for (ShareEntity share : indexEntity.getShares().values()) { // for each share in this Index
                        share.setNumberOfShares(
                                share.getNumberOfShares() * currentIndexValue / (currentIndexValue - reductionInIndexValue));
                    }

                }

            }
            if (!shareFound) {
                throw new IllegalArgumentException("Share not found in any index: " + shareDividendDTO.getShareName());
            }
        } finally {
            // Release all acquired locks
            for (ReentrantLock lock : acquiredLocks) {
                lock.unlock();
            }
        }
    }



    public List<IndexStateResponseDTO> getAllIndicesWithState() {
        return indexMap.values().stream()
                .map(indexMapper::toDto)
                .collect(Collectors.toList());
    }

    public IndexStateResponseDTO getIndexByName(String indexName) {
        IndexEntity indexEntity = indexMap.get(indexName);
        if (indexEntity != null) {
            return indexMapper.toDto(indexEntity);
        }
        return null;
    }

    public double calculateTotalIndexValue(IndexEntity indexEntity){
        double totalIndexValue = 0;
        for (ShareEntity shareEntity : indexEntity.getShares().values()) {
            totalIndexValue += shareEntity.getSharePrice() * shareEntity.getNumberOfShares();
        }
        return totalIndexValue;
    }

}



