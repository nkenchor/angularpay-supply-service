package io.angularpay.supply.models;

import io.angularpay.supply.domain.Amount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateCommodityUnitPriceCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotNull
    @Valid
    private Amount unitPrice;

    UpdateCommodityUnitPriceCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
