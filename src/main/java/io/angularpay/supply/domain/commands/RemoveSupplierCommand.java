package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.CommoditySupplier;
import io.angularpay.supply.domain.DeletedBy;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.helpers.CommandHelper;
import io.angularpay.supply.models.*;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.supply.domain.DeletedBy.INVESTOR;
import static io.angularpay.supply.domain.DeletedBy.TTL_SERVICE;
import static io.angularpay.supply.helpers.CommandHelper.*;
import static io.angularpay.supply.helpers.Helper.getAllParties;
import static io.angularpay.supply.helpers.Helper.getAllPartiesExceptActor;
import static io.angularpay.supply.models.UserNotificationType.SUPPLIER_DELETED_BY_SELF;
import static io.angularpay.supply.models.UserNotificationType.SUPPLIER_DELETED_BY_TTL;

@Service
public class RemoveSupplierCommand extends AbstractCommand<RemoveSupplierCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public RemoveSupplierCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter) {
        super("RemoveSupplierCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(RemoveSupplierCommandRequest request) {
        switch (request.getDeletedBy()) {
            case PLATFORM:
            case TTL_SERVICE:
                return request.getAuthenticatedUser().getUserReference();
            default:
                return this.commandHelper.getSupplyOwner(request.getRequestReference(), request.getSupplyReference());
        }
    }

    @Override
    protected GenericCommandResponse handle(RemoveSupplierCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String investmentReference = request.getSupplyReference();
        validRequestStatusAndInvestmentExists(found, investmentReference);
        Supplier<GenericCommandResponse> supplier = () -> removeInvestor(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse removeInvestor(RemoveSupplierCommandRequest request) {
        SupplyRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        found.getSuppliers().forEach(x-> {
            if (request.getSupplyReference().equalsIgnoreCase(x.getReference())) {
                validateInvestmentStatusOrThrow(x);
                x.setDeleted(true);
                x.setDeletedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
                x.setDeletedBy(request.getDeletedBy());
            }
        });
        SupplyRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .supplyRequest(response)
                .itemReference(request.getSupplyReference())
                .build();
    }

    @Override
    protected List<ErrorObject> validate(RemoveSupplierCommandRequest request) {
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
        DeletedBy deletedBy = commandResponse.getSupplyRequest().getSuppliers().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(commandResponse.getItemReference()))
                .findFirst()
                .map(CommoditySupplier::getDeletedBy)
                .orElse(TTL_SERVICE);
        return deletedBy == INVESTOR ? SUPPLIER_DELETED_BY_SELF : SUPPLIER_DELETED_BY_TTL;
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return this.getUserNotificationType(commandResponse) == SUPPLIER_DELETED_BY_SELF ?
                getAllPartiesExceptActor(commandResponse.getSupplyRequest(), commandResponse.getItemReference()) :
                getAllParties(commandResponse.getSupplyRequest());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, SupplyRequest> parameters) throws JsonProcessingException {
        String summary;
        Optional<String> optional = parameters.getCommandResponse().getSupplyRequest().getSuppliers().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(parameters.getCommandResponse().getItemReference()))
                .map(CommoditySupplier::getUserReference)
                .findFirst();
        if (optional.isPresent() && optional.get().equalsIgnoreCase(parameters.getUserReference())) {
            summary = "the comment you made on a Supply Request post, was deleted";
        } else {
            summary = "someone's comment on a Supply Request post that you commented on, was deleted";
        }

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
}
