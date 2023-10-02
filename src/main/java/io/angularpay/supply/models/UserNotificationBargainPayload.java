package io.angularpay.supply.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotificationBargainPayload {

    @JsonProperty("request_reference")
    private String requestReference;

    @JsonProperty("bargain_reference")
    private String bargainReference;
}
