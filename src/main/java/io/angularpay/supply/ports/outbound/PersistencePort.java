package io.angularpay.supply.ports.outbound;

import io.angularpay.supply.domain.RequestStatus;
import io.angularpay.supply.domain.SupplyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    SupplyRequest createRequest(SupplyRequest request);
    SupplyRequest updateRequest(SupplyRequest request);
    Optional<SupplyRequest> findRequestByReference(String reference);
    Page<SupplyRequest> listRequests(Pageable pageable);
    Page<SupplyRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses);
    Page<SupplyRequest> findRequestsByVerification(Pageable pageable, boolean verified);
    Page<SupplyRequest> findByBuyerUserReference(Pageable pageable, String userReference);
    long getCountByVerificationStatus(boolean verified);
    long getCountByRequestStatus(RequestStatus status);
    long getTotalCount();
}
