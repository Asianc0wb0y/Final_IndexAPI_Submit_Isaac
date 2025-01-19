package de.solactive.challenge.indexapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShareDividendDTO {

    @NotBlank(message = "Share name cannot be blank")
    private String shareName;
    @Positive(message = "Dividend must be positive")
    private double dividend;

    public ShareDividendDTO(String shareName, double dividend) {
        this.shareName = shareName;
        this.dividend = dividend;
    }


}
