package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.InvestmentStatus;
import io.angularpay.supply.domain.InvestmentTransactionStatus;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.CommandException;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.helpers.CommandHelper;
import io.angularpay.supply.models.GenericCommandResponse;
import io.angularpay.supply.models.MakePaymentCommandRequest;
import io.angularpay.supply.models.ResourceReferenceResponse;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.supply.exceptions.ErrorCode.REQUEST_COMPLETED_ERROR;
import static io.angularpay.supply.exceptions.ErrorCode.REQUEST_REMOVED_ERROR;
import static io.angularpay.supply.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.supply.helpers.CommandHelper.validRequestStatusAndInvestmentExists;

@Service
public class MakePaymentCommand extends AbstractCommand<MakePaymentCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public MakePaymentCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("MakePaymentCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(MakePaymentCommandRequest request) {
        return this.commandHelper.getSupplyOwner(request.getRequestReference(), request.getSupplyReference());
    }

    @Override
    protected GenericCommandResponse handle(MakePaymentCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String investmentReference = request.getSupplyReference();
        validRequestStatusAndInvestmentExists(found, investmentReference);
        Supplier<GenericCommandResponse> supplier = () -> makePayment(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse makePayment(MakePaymentCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String transactionReference = UUID.randomUUID().toString();
        found.getSuppliers().forEach(x -> {
            if (request.getSupplyReference().equalsIgnoreCase(x.getReference())) {
                if (x.isDeleted()) {
                    throw CommandException.builder()
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .errorCode(REQUEST_REMOVED_ERROR)
                            .message(REQUEST_REMOVED_ERROR.getDefaultMessage())
                            .build();
                }
                if (Objects.nonNull(x.getInvestmentStatus()) && x.getInvestmentStatus().getStatus() == InvestmentTransactionStatus.SUCCESSFUL) {
                    throw CommandException.builder()
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .errorCode(REQUEST_COMPLETED_ERROR)
                            .message(REQUEST_COMPLETED_ERROR.getDefaultMessage())
                            .build();
                }
                if (Objects.isNull(x.getInvestmentStatus())) {
                    x.setInvestmentStatus(InvestmentStatus.builder().status(InvestmentTransactionStatus.PENDING).build());
                }
                // TODO: integrate with transaction service
                //  all of these details should come from transaction service
                x.getInvestmentStatus().setTransactionReference(transactionReference);
                x.getInvestmentStatus().setTransactionDatetime(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
                x.getInvestmentStatus().setStatus(InvestmentTransactionStatus.SUCCESSFUL);
            }
        });
        SupplyRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .itemReference(transactionReference)
                .supplyRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(MakePaymentCommandRequest request) {
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
        return new ResourceReferenceResponse(genericCommandResponse.getItemReference());
    }
}
