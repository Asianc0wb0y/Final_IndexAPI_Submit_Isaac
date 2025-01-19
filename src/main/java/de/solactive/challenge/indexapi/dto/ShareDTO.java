package de.solactive.challenge.indexapi.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShareDTO {

    @NotBlank(message = "Share name cannot be blank")
    private String shareName;

    @Positive(message = "Share price must be positive")
    private double sharePrice;

    @Positive(message = "Number of shares must be positive")
    private double numberOfShares;


    // Constructor

    public ShareDTO(String shareName, double sharePrice, double numberOfShares) {
        this.shareName = shareName;
        this.sharePrice = sharePrice;
        this.numberOfShares = numberOfShares;
    }

}
