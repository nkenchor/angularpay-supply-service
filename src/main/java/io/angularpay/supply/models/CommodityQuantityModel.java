package io.angularpay.supply.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommodityQuantityModel {

    @NotEmpty
    private int quantity;
}
