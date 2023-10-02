package io.angularpay.supply.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.configurations.AngularPayConfiguration;
import io.angularpay.supply.domain.CommoditySupplier;
import io.angularpay.supply.domain.Offer;
import io.angularpay.supply.domain.RequestStatus;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.CommandException;
import io.angularpay.supply.exceptions.ErrorCode;
import io.angularpay.supply.models.GenericCommandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.angularpay.supply.domain.InvestmentTransactionStatus.SUCCESSFUL;
import static io.angularpay.supply.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CommandHelper {

    private final MongoAdapter mongoAdapter;
    private final ObjectMapper mapper;
    private final AngularPayConfiguration configuration;

    public GenericCommandResponse executeAcid(Supplier<GenericCommandResponse> supplier) {
        int maxRetry = this.configuration.getMaxUpdateRetry();
        OptimisticLockingFailureException optimisticLockingFailureException;
        int counter = 0;
        //noinspection ConstantConditions
        do {
            try {
                return supplier.get();
            } catch (OptimisticLockingFailureException exception) {
                if (counter++ >= maxRetry) throw exception;
                optimisticLockingFailureException = exception;
            }
        }
        while (Objects.nonNull(optimisticLockingFailureException));
        throw optimisticLockingFailureException;
    }

    public String getRequestOwner(String requestReference) {
        SupplyRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        return found.getBuyer().getUserReference();
    }

    private static CommandException commandException(HttpStatus status, ErrorCode errorCode) {
        return CommandException.builder()
                .status(status)
                .errorCode(errorCode)
                .message(errorCode.getDefaultMessage())
                .build();
    }

    public String getSupplyOwner(String requestReference, String supplyReference) {
        SupplyRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        if (CollectionUtils.isEmpty(found.getSuppliers())) return "";
        return found.getSuppliers().stream()
                .filter(x -> supplyReference.equalsIgnoreCase(x.getReference()))
                .map(CommoditySupplier::getUserReference)
                .findFirst()
                .orElse("");
    }

    public String getBargainOwner(String requestReference, String bargainReference) {
        SupplyRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        if (Objects.isNull(found.getBargain()) || CollectionUtils.isEmpty(found.getBargain().getOffers())) return "";
        return found.getBargain().getOffers().stream()
                .filter(x -> bargainReference.equalsIgnoreCase(x.getReference()))
                .map(Offer::getUserReference)
                .findFirst()
                .orElse("");
    }

    public <T> SupplyRequest updateProperty(SupplyRequest supplyRequest, Supplier<T> getter, Consumer<T> setter) {
        setter.accept(getter.get());
        return this.mongoAdapter.updateRequest(supplyRequest);
    }

    public <T> SupplyRequest addItemToCollection(SupplyRequest supplyRequest, T newProperty, Supplier<List<T>> collectionGetter, Consumer<List<T>> collectionSetter) {
        if (CollectionUtils.isEmpty(collectionGetter.get())) {
            collectionSetter.accept(new ArrayList<>());
        }
        collectionGetter.get().add(newProperty);
        return this.mongoAdapter.updateRequest(supplyRequest);
    }

    public <T> String toJsonString(T t) throws JsonProcessingException {
        return this.mapper.writeValueAsString(t);
    }

    public static SupplyRequest getRequestByReferenceOrThrow(MongoAdapter mongoAdapter, String requestReference) {
        return mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
    }

    public static void validRequestStatusAndBargainExists(SupplyRequest found, String bargainReference) {
        validRequestStatusOrThrow(found);
        if (Objects.isNull(found.getBargain()) || CollectionUtils.isEmpty(found.getBargain().getOffers())) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
        if (found.getBargain().getOffers().stream().noneMatch(x -> bargainReference.equalsIgnoreCase(x.getReference()))) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
    }

    public static void validRequestStatusAndInvestmentExists(SupplyRequest found, String investmentReference) {
        validRequestStatusOrThrow(found);
        if (CollectionUtils.isEmpty(found.getSuppliers())) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
        if (found.getSuppliers().stream().noneMatch(x -> investmentReference.equalsIgnoreCase(x.getReference()))) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
    }

    public static void validRequestStatusOrThrow(SupplyRequest found) {
        if (found.getStatus() == RequestStatus.COMPLETED) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_COMPLETED_ERROR);
        }
        if (found.getStatus() == RequestStatus.CANCELLED) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_CANCELLED_ERROR);
        }
    }

    public static void validateInvestmentStatusOrThrow(CommoditySupplier commoditySupplier) {
        if (Objects.nonNull(commoditySupplier.getInvestmentStatus()) && commoditySupplier.getInvestmentStatus().getStatus() == SUCCESSFUL) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_COMPLETED_ERROR);
        }
    }
}
