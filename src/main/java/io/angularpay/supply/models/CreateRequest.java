
package io.angularpay.supply.models;

import io.angularpay.supply.domain.Commodity;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CreateRequest {

    @NotEmpty
    private String summary;

    @NotNull
    @Valid
    private Commodity commodity;
}
