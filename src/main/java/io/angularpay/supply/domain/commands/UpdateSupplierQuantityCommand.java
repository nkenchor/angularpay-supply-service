package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.InvestmentTransactionStatus;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.CommandException;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.helpers.CommandHelper;
import io.angularpay.supply.models.GenericCommandResponse;
import io.angularpay.supply.models.UpdateSupplierQuantityCommandRequest;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static io.angularpay.supply.exceptions.ErrorCode.REQUEST_REMOVED_ERROR;
import static io.angularpay.supply.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.supply.helpers.CommandHelper.validRequestStatusAndInvestmentExists;

@Service
public class UpdateSupplierQuantityCommand extends AbstractCommand<UpdateSupplierQuantityCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    
    public UpdateSupplierQuantityCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("UpdateSupplierQuantityCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateSupplierQuantityCommandRequest request) {
        return this.commandHelper.getSupplyOwner(request.getRequestReference(), request.getSupplyReference());
    }

    @Override
    protected GenericCommandResponse handle(UpdateSupplierQuantityCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String investmentReference = request.getSupplyReference();
        validRequestStatusAndInvestmentExists(found, investmentReference);
        Supplier<GenericCommandResponse> supplier = () -> updateInvestmentAmount(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse updateInvestmentAmount(UpdateSupplierQuantityCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        found.getSuppliers().forEach(x-> {
            if (request.getSupplyReference().equalsIgnoreCase(x.getReference())) {
                if (x.isDeleted() || (Objects.nonNull(x.getInvestmentStatus()) && x.getInvestmentStatus().getStatus() == InvestmentTransactionStatus.SUCCESSFUL)) {
                    throw CommandException.builder()
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .errorCode(REQUEST_REMOVED_ERROR)
                            .message(REQUEST_REMOVED_ERROR.getDefaultMessage())
                            .build();
                }
                x.getSupplying().setQuantity(request.getUpdateSupplierQuantityApiModel().getQuantity());
                x.setComment(request.getUpdateSupplierQuantityApiModel().getComment());
            }
        });
        SupplyRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .supplyRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateSupplierQuantityCommandRequest request) {
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
