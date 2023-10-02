
package io.angularpay.supply.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSupplierApiModel {

    private int quantity;

    @NotEmpty
    private String comment;
}
