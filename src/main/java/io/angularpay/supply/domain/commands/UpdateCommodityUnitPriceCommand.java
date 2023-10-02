package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.Amount;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.helpers.CommandHelper;
import io.angularpay.supply.models.GenericCommandResponse;
import io.angularpay.supply.models.GenericReferenceResponse;
import io.angularpay.supply.models.UpdateCommodityUnitPriceCommandRequest;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static io.angularpay.supply.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.supply.helpers.CommandHelper.validRequestStatusOrThrow;

@Slf4j
@Service
public class UpdateCommodityUnitPriceCommand extends AbstractCommand<UpdateCommodityUnitPriceCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public UpdateCommodityUnitPriceCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("UpdateCommodityUnitPriceCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateCommodityUnitPriceCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected GenericCommandResponse handle(UpdateCommodityUnitPriceCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        Supplier<GenericCommandResponse> supplier = () -> updateCommodityUnitPrice(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse updateCommodityUnitPrice(UpdateCommodityUnitPriceCommandRequest request) throws OptimisticLockingFailureException {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());

        BigDecimal totalAmount = new BigDecimal(found.getCommodity().getQuantity())
                .multiply(new BigDecimal(request.getUnitPrice().getValue()));
        found.getCommodity().setTotalAmount(Amount.builder()
                .value(totalAmount.toPlainString()).currency(request.getUnitPrice().getCurrency())
                .build());

        SupplyRequest response = this.commandHelper.updateProperty(found, request::getUnitPrice, found.getCommodity()::setUnitPrice);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .supplyRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateCommodityUnitPriceCommandRequest request) {
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
}
