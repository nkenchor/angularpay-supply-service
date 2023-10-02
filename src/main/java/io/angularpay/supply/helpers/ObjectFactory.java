package io.angularpay.supply.helpers;

import io.angularpay.supply.domain.Bargain;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.domain.RequestStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

import static io.angularpay.supply.common.Constants.SERVICE_CODE;
import static io.angularpay.supply.util.SequenceGenerator.generateRequestTag;

public class ObjectFactory {

    public static SupplyRequest pmtRequestWithDefaults() {
        return SupplyRequest.builder()
                .reference(UUID.randomUUID().toString())
                .serviceCode(SERVICE_CODE)
                .verified(false)
                .status(RequestStatus.ACTIVE)
                .requestTag(generateRequestTag())
                .suppliers(new ArrayList<>())
                .bargain(Bargain.builder()
                        .offers(new ArrayList<>())
                        .build())
                .build();
    }
}