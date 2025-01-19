package de.solactive.challenge.indexapi.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IndexDTO {
    @NotBlank(message = "Index name cannot be blank")
    private String indexName;
    //private double indexValue;

    @Valid
    @Size(min =2, message = "An index must have at least two shares")
    private List<ShareDTO> indexMembers;

    public IndexDTO() {

    }

    public IndexDTO(String indexName, List<ShareDTO> indexMembers) {
        this.indexName = indexName;
        this.indexMembers = indexMembers;
    }





}

