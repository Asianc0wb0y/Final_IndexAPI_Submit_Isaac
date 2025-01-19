package de.solactive.challenge.indexapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShareDeletionDTO {

    @NotBlank(message = "Share name cannot be blank")
    private String shareName;

    @NotBlank(message = "Index name cannot be blank")
    private String indexName;

    public ShareDeletionDTO(String shareName, String indexName) {
        this.shareName = shareName;
        this.indexName = indexName;
    }
}
