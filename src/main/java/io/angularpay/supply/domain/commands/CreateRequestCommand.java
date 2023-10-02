package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.Amount;
import io.angularpay.supply.domain.Buyer;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.helpers.CommandHelper;
import io.angularpay.supply.models.CreateRequestCommandRequest;
import io.angularpay.supply.models.GenericCommandResponse;
import io.angularpay.supply.models.GenericReferenceResponse;
import io.angularpay.supply.models.ResourceReferenceResponse;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static io.angularpay.supply.helpers.ObjectFactory.pmtRequestWithDefaults;

@Slf4j
@Service
public class CreateRequestCommand extends AbstractCommand<CreateRequestCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public CreateRequestCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("CreateRequestCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(CreateRequestCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected GenericCommandResponse handle(CreateRequestCommandRequest request) {
        BigDecimal totalAmount = new BigDecimal(request.getCreateRequest().getCommodity().getQuantity())
                .multiply(new BigDecimal(request.getCreateRequest().getCommodity().getUnitPrice().getValue()));
        request.getCreateRequest().getCommodity().setTotalAmount(Amount.builder()
                .value(totalAmount.toPlainString()).currency(request.getCreateRequest().getCommodity().getUnitPrice().getCurrency())
                .build());

        SupplyRequest supplyRequestWithDefaults = pmtRequestWithDefaults();
        SupplyRequest withOtherDetails = supplyRequestWithDefaults.toBuilder()
                .summary(request.getCreateRequest().getSummary())
                .commodity(request.getCreateRequest().getCommodity())
                .buyer(Buyer.builder()
                        .userReference(request.getAuthenticatedUser().getUserReference())
                        .build())
                .build();
        SupplyRequest response = this.mongoAdapter.createRequest(withOtherDetails);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .supplyRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(CreateRequestCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public String convertToUpdatesMessage(SupplyRequest supplyRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(supplyRequest);
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }

    @Override
    public ResourceReferenceResponse map(GenericCommandResponse genericCommandResponse) {
        return new ResourceReferenceResponse(genericCommandResponse.getRequestReference());
    }
}
