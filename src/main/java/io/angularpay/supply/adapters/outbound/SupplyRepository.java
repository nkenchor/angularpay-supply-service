package io.angularpay.supply.adapters.outbound;

import io.angularpay.supply.domain.RequestStatus;
import io.angularpay.supply.domain.SupplyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends MongoRepository<SupplyRequest, String> {

    Optional<SupplyRequest> findByReference(String reference);
    Page<SupplyRequest> findAll(Pageable pageable);
    Page<SupplyRequest> findByStatusIn(Pageable pageable, List<RequestStatus> statuses);
    Page<SupplyRequest> findByVerified(Pageable pageable, boolean verified);
    Page<SupplyRequest> findAByBuyerUserReference(Pageable pageable, String userReference);
    long countByVerified(boolean verified);
    long countByStatus(RequestStatus status);
}
