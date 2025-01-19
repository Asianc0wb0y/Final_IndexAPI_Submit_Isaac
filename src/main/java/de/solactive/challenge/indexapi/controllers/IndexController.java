package de.solactive.challenge.indexapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.solactive.challenge.indexapi.services.IndexService;
import de.solactive.challenge.indexapi.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class IndexController {


    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;

    }

    /**
     * POST /api/create - Creates a new index with the given shares.
     *
     * @param indexDTO The index information provided in the request body.
     * @return HTTP 201 (Created), 409 (Conflict), or 400 (Bad Request).
     */
    @PostMapping("/create")
    public ResponseEntity<String> createIndex(@Valid @RequestBody IndexDTO indexDTO) {



        boolean isCreated = indexService.createIndex(indexDTO);
        if (isCreated) {
           // Index created successfully
            return ResponseEntity.status(201).build();
        } else {
            // Index already exists
            return ResponseEntity.status(409).build();
        }
    }

    /**
     * POST /api/indexAdjustment - Adjust the index (addition, deletion, dividend).
     *
     * @param requestsDTO The adjustment request containing one of addition, deletion, or dividend operations.
     * @return HTTP 201, 200, 400, 401, 404, or 405 depending on the operation result.
     */
    @PostMapping("/indexAdjustment")
    public ResponseEntity<String> indexAdjustment(@Valid @RequestBody IndexAdjustmentRequestsDTO requestsDTO) {
        try {

            if (requestsDTO.getAdditionOperation() != null) {
                // perform addition
               if ( indexService.addShareToIndex(requestsDTO.getAdditionOperation())) {
                   return ResponseEntity.status(201).build(); //201 Share added successfully
               } else {
                   return ResponseEntity.status(202).build(); // 202 Failed: Share already exist
               }

            } else if (requestsDTO.getDeletionOperation() != null) {
                // perform deletion
                indexService.deleteShareFromIndex(requestsDTO.getDeletionOperation());
                return ResponseEntity.ok().build(); //200 ok
            } else if (requestsDTO.getDividendOperation() != null) {
                // perform dividen
                indexService.applyDividend(requestsDTO.getDividendOperation());
                return ResponseEntity.ok().build(); // 200 ok
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();  // 401 Unauthorized
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();  // 404 Not Found
        } catch (IllegalStateException e) {
            return ResponseEntity.status(405).build();  // 405 Method Not Allowed
        }

        return ResponseEntity.badRequest().build(); // Invalid request 400

    }

    /**
     * GET /api/indexState - Returns the state of all indices.
     *
     * @return List of all index states.
     */
    @GetMapping("/indexState")
    public ResponseEntity<Map<String, List<IndexStateResponseDTO>>> getAllIndicesWithState() {
        List<IndexStateResponseDTO> response = indexService.getAllIndicesWithState();
        return ResponseEntity.ok(Map.of("indexDetails", response)); // 200
    }

    /**
     * GET /api/indexState/{indexName} - Returns the state of a specific index.
     *
     * @param indexName The name of the index.
     * @return The state of the specified index or 404 (Not Found).
     */
    @GetMapping("/indexState/{indexName}")
    public ResponseEntity<IndexStateResponseDTO> getIndexByName(@PathVariable String indexName) {
        IndexStateResponseDTO responseDTO = indexService.getIndexByName(indexName);
        if (responseDTO != null) {
            return ResponseEntity.ok(responseDTO); // 200 ok
        } else {
            return ResponseEntity.status(404).body(null); // 404 not found
        }
    }





}
