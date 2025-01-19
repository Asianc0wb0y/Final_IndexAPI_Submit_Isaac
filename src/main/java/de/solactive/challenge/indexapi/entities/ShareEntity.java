package de.solactive.challenge.indexapi.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareEntity {

    private String shareName;
    private double sharePrice;
    private double numberOfShares;

    public ShareEntity(String shareName, double sharePrice, double numberOfShares) {
        this.shareName = shareName;
        this.sharePrice = sharePrice;
        this.numberOfShares = numberOfShares;
    }

}
