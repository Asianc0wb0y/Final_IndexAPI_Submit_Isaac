package de.solactive.challenge.indexapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShareAdditionDTO {

    @NotBlank(message = "Share name cannot be blank")
    private String shareName;

    @Positive(message = "Share price must be positive")
    private double sharePrice;

    @Positive(message = "Number of shares must be positive")
    private double numberOfShares;

    @NotBlank(message = "Index name cannot be blank")
    private String indexName;

    public ShareAdditionDTO(String shareName, double sharePrice, double numberOfShares, String indexName) {
        this.shareName = shareName;
        this.sharePrice = sharePrice;
        this.numberOfShares = numberOfShares;
        this.indexName = indexName;
    }

}
