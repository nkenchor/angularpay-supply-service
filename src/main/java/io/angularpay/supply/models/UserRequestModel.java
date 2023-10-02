
package io.angularpay.supply.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestModel {

    @JsonProperty("request_created_on")
    private String requestCreatedOn;
    @JsonProperty("request_reference")
    private String requestReference;
    @JsonProperty("user_reference")
    private String userReference;
}
