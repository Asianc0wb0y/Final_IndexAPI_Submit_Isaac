package de.solactive.challenge.indexapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class IndexAdjustmentRequestsDTO {

    @Valid
    private ShareAdditionDTO additionOperation;


    @Valid
    private ShareDeletionDTO deletionOperation;


    @Valid
    private ShareDividendDTO dividendOperation;

}
