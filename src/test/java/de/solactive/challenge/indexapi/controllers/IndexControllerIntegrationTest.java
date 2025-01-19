package de.solactive.challenge.indexapi.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IndexControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void integrationTest_FullFlow() throws Exception {
        // Step 1: Create Index
        mockMvc.perform(post("/api/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "indexName": "INDEX_1",
                      "indexMembers": [
                        { "shareName": "A.OQ", "sharePrice": 10.0, "numberOfShares": 20.0 },
                        { "shareName": "B.OQ", "sharePrice": 20.0, "numberOfShares": 30.0 },
                        { "shareName": "C.OQ", "sharePrice": 30.0, "numberOfShares": 40.0 },
                        { "shareName": "D.OQ", "sharePrice": 40.0, "numberOfShares": 50.0 }
                      ]
                    }
                    """))
                .andExpect(status().isCreated());

        // Step 2: Add a Share
        mockMvc.perform(post("/api/indexAdjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "additionOperation": {
                            "shareName": "E.OQ",
                            "sharePrice": 10.0,
                            "numberOfShares": 20.0,
                            "indexName": "INDEX_1"
                          }
                        }
                        """))
                .andExpect(status().isCreated());

        // Step 3: Delete a Share
        mockMvc.perform(post("/api/indexAdjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "deletionOperation": {
                            "shareName": "D.OQ",
                            "indexName": "INDEX_1"
                          }
                        }
                        """))
                .andExpect(status().isOk());

        // Step 4: Apply Dividend
        mockMvc.perform(post("/api/indexAdjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "dividendOperation": {
                            "shareName": "A.OQ",
                            "dividend": 2.0
                          }
                        }
                        """))
                .andExpect(status().isOk());


        // Step 5: Get Index State
        mockMvc.perform(get("/api/indexState"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexDetails[0].indexName").value("INDEX_1"))
                .andExpect(jsonPath("$.indexDetails[0].indexValue").value(closeTo(4000.0, 0.00001)))
                .andExpect(jsonPath("$.indexDetails[0].indexMembers[0].shareName").value("A.OQ"))
                .andExpect(jsonPath("$.indexDetails[0].indexMembers[1].shareName").value("B.OQ"))
                .andExpect(jsonPath("$.indexDetails[0].indexMembers[2].shareName").value("C.OQ"))
                .andExpect(jsonPath("$.indexDetails[0].indexMembers[3].shareName").value("E.OQ"));
    }
}

