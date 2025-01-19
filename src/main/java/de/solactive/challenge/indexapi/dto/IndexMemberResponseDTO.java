package de.solactive.challenge.indexapi.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({ "shareName", "sharePrice", "numberOfShares", "indexWeightPct", "indexValue" })
public class IndexMemberResponseDTO {


    private String shareName;
    private double sharePrice;
    private double numberOfShares;
    private double indexWeightPct;  // (indexValue / totalIndexValue) * 100
    private double indexValue;  // sharePrice * numberOfShares


    public IndexMemberResponseDTO(String shareName, double sharePrice, double numberOfShares, double indexWeightPct, double indexValue) {
        this.shareName = shareName;
        this.sharePrice = sharePrice;
        this.numberOfShares = numberOfShares;
        this.indexWeightPct = indexWeightPct;
        this.indexValue = indexValue;
    }
}
