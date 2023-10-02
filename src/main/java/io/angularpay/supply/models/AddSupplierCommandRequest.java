package io.angularpay.supply.models;

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
public class AddSupplierCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotNull
    @Valid
    private AddSupplierApiModel addSupplierApiModel;

    AddSupplierCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
