
package io.angularpay.supply.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AddBargainApiModel {

    @NotEmpty
    private String currency;

    @NotEmpty
    private String value;

    @NotEmpty
    private String comment;
}
