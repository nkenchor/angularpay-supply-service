package io.angularpay.supply.models;

import io.angularpay.supply.domain.RequestStatus;
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
public class UpdateRequestStatusCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotNull
    private RequestStatus status;

    UpdateRequestStatusCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
