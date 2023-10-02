
package io.angularpay.supply.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commodity {

    @NotEmpty
    private String description;

    @NotEmpty
    private String name;

    @NotNull
    private Integer quantity;

    @NotNull
    @Valid
    @JsonProperty("unit_price")
    private Amount unitPrice;

    @JsonProperty("total_amount")
    private Amount totalAmount;
}
