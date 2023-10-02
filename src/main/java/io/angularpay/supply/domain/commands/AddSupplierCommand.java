package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.configurations.AngularPayConfiguration;
import io.angularpay.supply.domain.*;
import io.angularpay.supply.exceptions.CommandException;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.helpers.CommandHelper;
import io.angularpay.supply.models.*;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.supply.exceptions.ErrorCode.TARGET_AMOUNT_BOUNDS_ERROR;
import static io.angularpay.supply.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.supply.helpers.CommandHelper.validRequestStatusOrThrow;
import static io.angularpay.supply.helpers.Helper.getAllPartiesExceptActor;
import static io.angularpay.supply.models.UserNotificationType.PEER_INVESTOR_ADDED;
import static io.angularpay.supply.models.UserNotificationType.SOLO_INVESTOR_ADDED;

@Service
public class AddSupplierCommand extends AbstractCommand<AddSupplierCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse>,
        TTLPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    private final AngularPayConfiguration configuration;

    public AddSupplierCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter, AngularPayConfiguration configuration) {
        super("AddSupplierCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
        this.configuration = configuration;
    }

    @Override
    protected String getResourceOwner(AddSupplierCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected GenericCommandResponse handle(AddSupplierCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        Supplier<GenericCommandResponse> supplier = () -> addSupplier(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse addSupplier(AddSupplierCommandRequest request) throws OptimisticLockingFailureException {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        int targetQuantity = found.getCommodity().getQuantity();
        int runningTotal = found.getSuppliers().stream()
                .filter(x -> !x.isDeleted())
                .map(x -> x.getSupplying().getQuantity()).reduce(0, Integer::sum);
        if (runningTotal < targetQuantity &&
                (runningTotal + request.getAddSupplierApiModel().getQuantity()) <= targetQuantity) {

            Amount totalAmount = Amount.builder()
                    .value(new BigDecimal(request.getAddSupplierApiModel().getQuantity())
                            .multiply(new BigDecimal(found.getCommodity().getUnitPrice().getValue())).toPlainString())
                    .currency(found.getCommodity().getUnitPrice().getCurrency())
                    .build();

            CommoditySupplier supplier = CommoditySupplier.builder()
                    .supplying(Supplying.builder().quantity(request.getAddSupplierApiModel().getQuantity())
                            .totalAmount(totalAmount).build())
                    .comment(request.getAddSupplierApiModel().getComment())
                    .reference(UUID.randomUUID().toString())
                    .userReference(request.getAuthenticatedUser().getUserReference())
                    .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                    .investmentStatus(InvestmentStatus.builder()
                            .status(InvestmentTransactionStatus.PENDING)
                            .build())
                    .build();
            SupplyRequest response = this.commandHelper.addItemToCollection(found, supplier, found::getSuppliers, found::setSuppliers);
            return GenericCommandResponse.builder()
                    .requestReference(found.getReference())
                    .itemReference(supplier.getReference())
                    .supplyRequest(response)
                    .build();
        }
        throw CommandException.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .errorCode(TARGET_AMOUNT_BOUNDS_ERROR)
                .message(TARGET_AMOUNT_BOUNDS_ERROR.getDefaultMessage())
                .build();
    }

    @Override
    protected List<ErrorObject> validate(AddSupplierCommandRequest request) {
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
    public UserNotificationType getUserNotificationType(GenericCommandResponse commandResponse) {
        SupplyRequest request = commandResponse.getSupplyRequest();
        Optional<CommoditySupplier> optionalInvestor = request.getSuppliers().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(commandResponse.getItemReference()))
                .findFirst();

        if (optionalInvestor.isEmpty()) {
            return PEER_INVESTOR_ADDED;
        } else {
            int quantity = optionalInvestor.get().getSupplying().getQuantity();
            return quantity == request.getCommodity().getQuantity() ? SOLO_INVESTOR_ADDED : PEER_INVESTOR_ADDED;
        }
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return getAllPartiesExceptActor(commandResponse.getSupplyRequest(), commandResponse.getItemReference());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, SupplyRequest> parameters) throws JsonProcessingException {
        Optional<CommoditySupplier> optional = parameters.getCommandResponse().getSupplyRequest().getSuppliers().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(parameters.getCommandResponse().getItemReference()))
                .findFirst();

        int quantity;
        if (optional.isEmpty()) {
            quantity = 0;
        } else {
            quantity = optional.get().getSupplying().getQuantity();
        }

        String template;
        if (parameters.getCommandResponse().getSupplyRequest().getBuyer().getUserReference()
                .equalsIgnoreCase(parameters.getUserReference())) {
            template = "someone wants to supply %d units on your Supply post";
        } else {
            template = "someone else wants to supply %d units on a Supply post that you commented on";
        }

        String summary = String.format(template, quantity);

        UserNotificationInvestmentPayload userNotificationInvestmentPayload = UserNotificationInvestmentPayload.builder()
                .requestReference(parameters.getCommandResponse().getRequestReference())
                .investmentReference(parameters.getCommandResponse().getItemReference())
                .build();
        String payload = mapper.writeValueAsString(userNotificationInvestmentPayload);

        String attributes = mapper.writeValueAsString(parameters.getRequest());

        UserNotification userNotification = UserNotification.builder()
                .reference(UUID.randomUUID().toString())
                .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .serviceCode(parameters.getRequest().getServiceCode())
                .userReference(parameters.getUserReference())
                .type(parameters.getType())
                .summary(summary)
                .payload(payload)
                .attributes(attributes)
                .build();

        return mapper.writeValueAsString(userNotification);
    }

    @Override
    public ResourceReferenceResponse map(GenericCommandResponse genericCommandResponse) {
        return new ResourceReferenceResponse(genericCommandResponse.getItemReference());
    }

    @Override
    public String convertToTTLMessage(SupplyRequest supplyRequest, GenericCommandResponse genericCommandResponse) throws JsonProcessingException {
        URI deletionLink = UriComponentsBuilder.fromUriString(configuration.getSelfUrl())
                .path("/supply/requests/")
                .path(genericCommandResponse.getRequestReference())
                .path("/suppliers/")
                .path(genericCommandResponse.getItemReference())
                .path("/ttl")
                .build().toUri();

        return this.commandHelper.toJsonString(TimeToLiveModel.builder()
                .serviceCode(supplyRequest.getServiceCode())
                .requestReference(supplyRequest.getReference())
                .investmentReference(genericCommandResponse.getItemReference())
                .requestCreatedOn(supplyRequest.getCreatedOn())
                .deletionLink(deletionLink.toString())
                .build());
    }
}
