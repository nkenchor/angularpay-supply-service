
package io.angularpay.supply.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplying {

    private int quantity;
    @JsonProperty("total_amount")
    private Amount totalAmount;
}
