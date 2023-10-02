package io.angularpay.supply.ports.inbound;

import io.angularpay.supply.domain.*;
import io.angularpay.supply.models.*;

import java.util.List;
import java.util.Map;

public interface RestApiPort {
    GenericReferenceResponse createScheduledRequest(String schedule, CreateRequest request, Map<String, String> headers);
    GenericReferenceResponse create(CreateRequest request, Map<String, String> headers);
    void updateSummary(String requestReference, SummaryModel summaryModel, Map<String, String> headers);
    void updateCommodityUnitPrice(String requestReference, Amount setUnitPrice, Map<String, String> headers);
    void updateCommodityQuantity(String requestReference, CommodityQuantityModel commodityQuantityModel, Map<String, String> headers);
    void updateVerificationStatus(String requestReference, boolean verified, Map<String, String> headers);
    GenericReferenceResponse addSupplier(String requestReference, AddSupplierApiModel addSupplierApiModel, Map<String, String> headers);
    void removeSupplier(String requestReference, String supplyReference, Map<String, String> headers);
    void removeSupplierTTL(String requestReference, String supplyReference, Map<String, String> headers);
    void removeSupplierPlatform(String requestReference, String supplyReference, Map<String, String> headers);
    GenericReferenceResponse addBargain(String requestReference, AddBargainApiModel addBargainApiModel, Map<String, String> headers);
    void acceptBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void rejectBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void deleteBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void updateSupplierQuantity(String requestReference, String supplyReference, UpdateSupplierQuantityApiModel updateSupplierQuantityApiModel, Map<String, String> headers);
    GenericReferenceResponse makePayment(String requestReference, String supplyReference, PaymentRequest paymentRequest, Map<String, String> headers);
    void updateRequestStatus(String requestReference, RequestStatusModel status, Map<String, String> headers);
    SupplyRequest getRequestByReference(String requestReference, Map<String, String> headers);
    List<SupplyRequest> getNewsfeedModel(int page, Map<String, String> headers);
    List<UserRequestModel> getUserRequests(int page, Map<String, String> headers);
    List<UserInvestmentModel> getUserInvestments(int page, Map<String, String> headers);
    List<SupplyRequest> getNewsfeedByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<SupplyRequest> getRequestListByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<SupplyRequest> getRequestListByVerification(int page, boolean verified, Map<String, String> headers);
    List<SupplyRequest> getRequestList(int page, Map<String, String> headers);
    List<Statistics> getStatistics(Map<String, String> headers);
}
