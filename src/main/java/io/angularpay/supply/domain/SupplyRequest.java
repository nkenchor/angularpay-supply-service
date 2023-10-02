
package io.angularpay.supply.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("supply_requests")
public class SupplyRequest {

    @Id
    private String id;
    @Version
    private int version;
    @JsonProperty("service_code")
    private String serviceCode;
    private boolean verified;
    @JsonProperty("verified_on")
    private String verifiedOn;
    private String summary;
    private Bargain bargain;
    @JsonProperty("created_on")
    private String createdOn;
    private Commodity commodity;
    private Buyer buyer;
    private List<CommoditySupplier> suppliers;
    @JsonProperty("last_modified")
    private String lastModified;
    private String reference;
    @JsonProperty("request_tag")
    private String requestTag;
    private RequestStatus status;
}
