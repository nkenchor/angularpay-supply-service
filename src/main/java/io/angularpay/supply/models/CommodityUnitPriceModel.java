package io.angularpay.supply.models;

import io.angularpay.supply.domain.Amount;
import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class CommodityUnitPriceModel {

    @NotEmpty
    private Amount amount;
}
