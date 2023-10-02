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
public class MakePaymentCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotEmpty
    private String supplyReference;

    @NotNull
    @Valid
    private PaymentRequest paymentRequest;

    MakePaymentCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
