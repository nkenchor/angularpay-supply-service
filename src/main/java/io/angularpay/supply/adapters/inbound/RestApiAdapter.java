package io.angularpay.supply.adapters.inbound;

import io.angularpay.supply.configurations.AngularPayConfiguration;
import io.angularpay.supply.domain.*;
import io.angularpay.supply.domain.commands.*;
import io.angularpay.supply.models.*;
import io.angularpay.supply.ports.inbound.RestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.angularpay.supply.domain.DeletedBy.*;
import static io.angularpay.supply.helpers.Helper.fromHeaders;

@RestController
@RequestMapping("/supply/requests")
@RequiredArgsConstructor
public class RestApiAdapter implements RestApiPort {

    private final AngularPayConfiguration configuration;

    private final CreateRequestCommand createRequestCommand;
    private final UpdateSupplySummaryCommand updateSupplySummaryCommand;
    private final UpdateCommodityUnitPriceCommand updateCommodityUnitPriceCommand;
    private final UpdateCommodityQuantityCommand updateCommodityQuantityCommand;
    private final UpdateVerificationStatusCommand updateVerificationStatusCommand;
    private final AddSupplierCommand addSupplierCommand;
    private final RemoveSupplierCommand removeSupplierCommand;
    private final AddBargainCommand addBargainCommand;
    private final AcceptBargainCommand acceptBargainCommand;
    private final RejectBargainCommand rejectBargainCommand;
    private final DeleteBargainCommand deleteBargainCommand;
    private final UpdateSupplierQuantityCommand updateSupplierQuantityCommand;
    private final MakePaymentCommand makePaymentCommand;
    private final UpdateRequestStatusCommand updateRequestStatusCommand;
    private final GetRequestByReferenceCommand getRequestByReferenceCommand;
    private final GetNewsfeedCommand getNewsfeedCommand;
    private final GetUserRequestsCommand getUserRequestsCommand;
    private final GetUserInvestmentsCommand getUserInvestmentsCommand;
    private final GetNewsfeedByStatusCommand getNewsfeedByStatusCommand;
    private final GetRequestListByStatusCommand getRequestListByStatusCommand;
    private final GetRequestListByVerificationCommand getRequestListByVerificationCommand;
    private final GetRequestListCommand getRequestListCommand;
    private final ScheduledRequestCommand scheduledRequestCommand;
    private final GetStatisticsCommand getStatisticsCommand;

    @PostMapping("/schedule/{schedule}")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse createScheduledRequest(
            @PathVariable String schedule,
            @RequestBody CreateRequest request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ScheduledRequestCommandRequest scheduledRequestCommandRequest = ScheduledRequestCommandRequest.builder()
                .runAt(schedule)
                .createRequest(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return scheduledRequestCommand.execute(scheduledRequestCommandRequest);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse create(
            @RequestBody CreateRequest request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        CreateRequestCommandRequest createRequestCommandRequest = CreateRequestCommandRequest.builder()
                .createRequest(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return createRequestCommand.execute(createRequestCommandRequest);
    }

    @PutMapping("/{requestReference}/summary")
    @Override
    public void updateSummary(
            @PathVariable String requestReference,
            @RequestBody SummaryModel summaryModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateSupplySummaryCommandRequest updateSupplySummaryCommandRequest = UpdateSupplySummaryCommandRequest.builder()
                .requestReference(requestReference)
                .summary(summaryModel.getSummary())
                .authenticatedUser(authenticatedUser)
                .build();
        updateSupplySummaryCommand.execute(updateSupplySummaryCommandRequest);
    }

    @PutMapping("{requestReference}/commodity-unit-price")
    @Override
    public void updateCommodityUnitPrice(
            @PathVariable String requestReference,
            @RequestBody Amount setUnitPrice,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateCommodityUnitPriceCommandRequest updateCommodityUnitPriceCommandRequest = UpdateCommodityUnitPriceCommandRequest.builder()
                .requestReference(requestReference)
                .unitPrice(setUnitPrice)
                .authenticatedUser(authenticatedUser)
                .build();
        updateCommodityUnitPriceCommand.execute(updateCommodityUnitPriceCommandRequest);
    }

    @PutMapping("{requestReference}/commodity-quantity")
    @Override
    public void updateCommodityQuantity(
            @PathVariable String requestReference,
            @RequestBody CommodityQuantityModel commodityQuantityModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateCommodityQuantityCommandRequest updateCommodityQuantityCommandRequest = UpdateCommodityQuantityCommandRequest.builder()
                .requestReference(requestReference)
                .quantity(commodityQuantityModel.getQuantity())
                .authenticatedUser(authenticatedUser)
                .build();
        updateCommodityQuantityCommand.execute(updateCommodityQuantityCommandRequest);
    }

    @PutMapping("/{requestReference}/verify/{verified}")
    @Override
    public void updateVerificationStatus(
            @PathVariable String requestReference,
            @PathVariable boolean verified,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateVerificationStatusCommandRequest updateVerificationStatusCommandRequest = UpdateVerificationStatusCommandRequest.builder()
                .requestReference(requestReference)
                .verified(verified)
                .authenticatedUser(authenticatedUser)
                .build();
        updateVerificationStatusCommand.execute(updateVerificationStatusCommandRequest);
    }

    @PostMapping("/{requestReference}/suppliers")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse addSupplier(
            @PathVariable String requestReference,
            @RequestBody AddSupplierApiModel addSupplierApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        AddSupplierCommandRequest addSupplierCommandRequest = AddSupplierCommandRequest.builder()
                .requestReference(requestReference)
                .addSupplierApiModel(addSupplierApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return addSupplierCommand.execute(addSupplierCommandRequest);
    }

    @DeleteMapping("/{requestReference}/suppliers/{supplyReference}")
    @Override
    public void removeSupplier(
            @PathVariable String requestReference,
            @PathVariable String supplyReference,
            @RequestHeader Map<String, String> headers) {
        removeSupplier(requestReference, supplyReference, headers, INVESTOR);
    }

    @DeleteMapping("/{requestReference}/suppliers/{supplyReference}/ttl")
    @Override
    public void removeSupplierTTL(
            @PathVariable String requestReference,
            @PathVariable String supplyReference,
            @RequestHeader Map<String, String> headers) {
        removeSupplier(requestReference, supplyReference, headers, TTL_SERVICE);
    }

    @DeleteMapping("/{requestReference}/suppliers/{supplyReference}/platform")
    @Override
    public void removeSupplierPlatform(
            @PathVariable String requestReference,
            @PathVariable String supplyReference,
            @RequestHeader Map<String, String> headers) {
        removeSupplier(requestReference, supplyReference, headers, PLATFORM);
    }

    private void removeSupplier(
            String requestReference,
            String supplyReference,
            Map<String, String> headers,
            DeletedBy deletedBy) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RemoveSupplierCommandRequest removeSupplierCommandRequest = RemoveSupplierCommandRequest.builder()
                .requestReference(requestReference)
                .supplyReference(supplyReference)
                .deletedBy(deletedBy)
                .authenticatedUser(authenticatedUser)
                .build();
        removeSupplierCommand.execute(removeSupplierCommandRequest);
    }

    @PostMapping("{requestReference}/bargains")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse addBargain(
            @PathVariable String requestReference,
            @RequestBody AddBargainApiModel addBargainApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        AddBargainCommandRequest addBargainCommandRequest = AddBargainCommandRequest.builder()
                .requestReference(requestReference)
                .addBargainApiModel(addBargainApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return addBargainCommand.execute(addBargainCommandRequest);
    }

    @PutMapping("{requestReference}/bargains/{bargainReference}")
    @Override
    public void acceptBargain(
            @PathVariable String requestReference,
            @PathVariable String bargainReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        AcceptBargainCommandRequest acceptBargainCommandRequest = AcceptBargainCommandRequest.builder()
                .requestReference(requestReference)
                .bargainReference(bargainReference)
                .authenticatedUser(authenticatedUser)
                .build();
        acceptBargainCommand.execute(acceptBargainCommandRequest);
    }

    @DeleteMapping("{requestReference}/bargains/{bargainReference}")
    @Override
    public void rejectBargain(
            @PathVariable String requestReference,
            @PathVariable String bargainReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RejectBargainCommandRequest rejectBargainCommandRequest = RejectBargainCommandRequest.builder()
                .requestReference(requestReference)
                .bargainReference(bargainReference)
                .authenticatedUser(authenticatedUser)
                .build();
        rejectBargainCommand.execute(rejectBargainCommandRequest);
    }

    @DeleteMapping("{requestReference}/bargains/{bargainReference}/delete")
    @Override
    public void deleteBargain(
            @PathVariable String requestReference,
            @PathVariable String bargainReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        DeleteBargainCommandRequest deleteBargainCommandRequest = DeleteBargainCommandRequest.builder()
                .requestReference(requestReference)
                .bargainReference(bargainReference)
                .authenticatedUser(authenticatedUser)
                .build();
        deleteBargainCommand.execute(deleteBargainCommandRequest);
    }

    @PutMapping("/{requestReference}/investors/{supplyReference}/quantity")
    @Override
    public void updateSupplierQuantity(
            @PathVariable String requestReference,
            @PathVariable String supplyReference,
            @RequestBody UpdateSupplierQuantityApiModel updateSupplierQuantityApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateSupplierQuantityCommandRequest updateSupplierQuantityCommandRequest = UpdateSupplierQuantityCommandRequest.builder()
                .requestReference(requestReference)
                .supplyReference(supplyReference)
                .updateSupplierQuantityApiModel(updateSupplierQuantityApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        updateSupplierQuantityCommand.execute(updateSupplierQuantityCommandRequest);
    }

    @PostMapping("{requestReference}/suppliers/{supplyReference}/payment")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse makePayment(
            @PathVariable String requestReference,
            @PathVariable String supplyReference,
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        MakePaymentCommandRequest makePaymentCommandRequest = MakePaymentCommandRequest.builder()
                .requestReference(requestReference)
                .supplyReference(supplyReference)
                .paymentRequest(paymentRequest)
                .authenticatedUser(authenticatedUser)
                .build();
        return makePaymentCommand.execute(makePaymentCommandRequest);
    }

    @PutMapping("/{requestReference}/status")
    @Override
    public void updateRequestStatus(
            @PathVariable String requestReference,
            @RequestBody RequestStatusModel status,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateRequestStatusCommandRequest updateRequestStatusCommandRequest = UpdateRequestStatusCommandRequest.builder()
                .requestReference(requestReference)
                .status(status.getStatus())
                .authenticatedUser(authenticatedUser)
                .build();
        updateRequestStatusCommand.execute(updateRequestStatusCommandRequest);
    }

    @GetMapping("/{requestReference}")
    @Override
    public SupplyRequest getRequestByReference(
            @PathVariable String requestReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetRequestByReferenceCommandRequest getRequestByReferenceCommandRequest = GetRequestByReferenceCommandRequest.builder()
                .requestReference(requestReference)
                .authenticatedUser(authenticatedUser)
                .build();
        return getRequestByReferenceCommand.execute(getRequestByReferenceCommandRequest);
    }

    @GetMapping("/list/newsfeed/page/{page}")
    @Override
    public List<SupplyRequest> getNewsfeedModel(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetRequestListCommandRequest genericGetRequestListCommandRequest = GenericGetRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getNewsfeedCommand.execute(genericGetRequestListCommandRequest);
    }

    @GetMapping("/list/user-request/page/{page}")
    @Override
    public List<UserRequestModel> getUserRequests(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetUserRequestsCommandRequest getUserRequestsCommandRequest = GetUserRequestsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getUserRequestsCommand.execute(getUserRequestsCommandRequest);
    }

    @GetMapping("/list/user-investment/page/{page}")
    @Override
    public List<UserInvestmentModel> getUserInvestments(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetUserInvestmentsCommandRequest getUserInvestmentsCommandRequest = GetUserInvestmentsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getUserInvestmentsCommand.execute(getUserInvestmentsCommandRequest);
    }

    @GetMapping("/list/newsfeed/page/{page}/filter/statuses/{statuses}")
    @ResponseBody
    @Override
    public List<SupplyRequest> getNewsfeedByStatus(
            @PathVariable int page,
            @PathVariable List<RequestStatus> statuses,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetByStatusCommandRequest genericGetByStatusCommandRequest = GenericGetByStatusCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .statuses(statuses)
                .build();
        return getNewsfeedByStatusCommand.execute(genericGetByStatusCommandRequest);
    }

    @GetMapping("/list/page/{page}/filter/statuses/{statuses}")
    @ResponseBody
    @Override
    public List<SupplyRequest> getRequestListByStatus(
            @PathVariable int page,
            @PathVariable List<RequestStatus> statuses,
            Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetByStatusCommandRequest genericGetByStatusCommandRequest = GenericGetByStatusCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .statuses(statuses)
                .build();
        return getRequestListByStatusCommand.execute(genericGetByStatusCommandRequest);
    }

    @GetMapping("/list/page/{page}/filter/verified/{verified}")
    @ResponseBody
    @Override
    public List<SupplyRequest> getRequestListByVerification(
            @PathVariable int page,
            @PathVariable boolean verified,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetRequestListByVerificationCommandRequest getRequestListByVerificationCommandRequest = GetRequestListByVerificationCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .verified(verified)
                .build();
        return getRequestListByVerificationCommand.execute(getRequestListByVerificationCommandRequest);
    }

    @GetMapping("/list/page/{page}")
    @ResponseBody
    @Override
    public List<SupplyRequest> getRequestList(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetRequestListCommandRequest genericGetRequestListCommandRequest = GenericGetRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getRequestListCommand.execute(genericGetRequestListCommandRequest);
    }

    @GetMapping("/statistics")
    @ResponseBody
    @Override
    public List<Statistics> getStatistics(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetStatisticsCommandRequest getStatisticsCommandRequest = GetStatisticsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .build();
        return getStatisticsCommand.execute(getStatisticsCommandRequest);
    }
}
