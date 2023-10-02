package io.angularpay.supply.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GenericGetRequestListCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private Paging paging;

    GenericGetRequestListCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
