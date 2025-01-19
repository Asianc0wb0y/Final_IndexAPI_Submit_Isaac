package de.solactive.challenge.indexapi.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class IndexEntity {

    private String indexName;
   //  private double indexValue --- should not be stored as it will be calculated and presented in DTO
    private Map<String, ShareEntity> shares;


    public IndexEntity(String indexName, Map<String, ShareEntity> shares) {
        this.indexName = indexName;
        this.shares = shares;
    }


}
