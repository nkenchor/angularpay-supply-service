package io.angularpay.supply.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.angularpay.supply.domain.Amount;
import lombok.Data;

@Data
public class BargainModel {

    @JsonProperty("unit_price")
    private Amount setUnitPrice;
}
