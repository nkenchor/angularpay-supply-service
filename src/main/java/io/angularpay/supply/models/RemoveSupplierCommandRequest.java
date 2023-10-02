package io.angularpay.supply.models;

import io.angularpay.supply.domain.DeletedBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RemoveSupplierCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotEmpty
    private String supplyReference;

    @NotNull
    private DeletedBy deletedBy;

    RemoveSupplierCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
