
package io.angularpay.supply.models;

import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.domain.commands.SupplyRequestSupplier;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class GenericCommandResponse extends GenericReferenceResponse implements SupplyRequestSupplier {

    private final String requestReference;
    private final String itemReference;
    private final SupplyRequest supplyRequest;
}
