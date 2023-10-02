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
public class CreateRequestCommandRequest extends AccessControl {

    @NotNull
    @Valid
    private CreateRequest createRequest;

    CreateRequestCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
