package de.solactive.challenge.indexapi.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class IndexStateResponseDTO {


    private String indexName;
    private double indexValue;
    private List<IndexMemberResponseDTO> indexMembers;

    public IndexStateResponseDTO(String indexName, double indexValue, List<IndexMemberResponseDTO> indexMembers) {
        this.indexName = indexName;
        this.indexValue = indexValue;
        this.indexMembers = indexMembers;

    }

}
