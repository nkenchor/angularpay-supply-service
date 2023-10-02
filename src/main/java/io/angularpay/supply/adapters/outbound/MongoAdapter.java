package io.angularpay.supply.adapters.outbound;

import io.angularpay.supply.domain.RequestStatus;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final SupplyRepository supplyRepository;

    @Override
    public SupplyRequest createRequest(SupplyRequest request) {
        request.setCreatedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return supplyRepository.save(request);
    }

    @Override
    public SupplyRequest updateRequest(SupplyRequest request) {
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return supplyRepository.save(request);
    }

    @Override
    public Optional<SupplyRequest> findRequestByReference(String reference) {
        return supplyRepository.findByReference(reference);
    }

    @Override
    public Page<SupplyRequest> listRequests(Pageable pageable) {
        return supplyRepository.findAll(pageable);
    }

    @Override
    public Page<SupplyRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses) {
        return supplyRepository.findByStatusIn(pageable, statuses);
    }

    @Override
    public Page<SupplyRequest> findRequestsByVerification(Pageable pageable, boolean verified) {
        return supplyRepository.findByVerified(pageable, verified);
    }

    @Override
    public Page<SupplyRequest> findByBuyerUserReference(Pageable pageable, String userReference) {
        return supplyRepository.findAByBuyerUserReference(pageable, userReference);
    }

    @Override
    public long getCountByVerificationStatus(boolean verified) {
        return supplyRepository.countByVerified(verified);
    }

    @Override
    public long getCountByRequestStatus(RequestStatus status) {
        return supplyRepository.countByStatus(status);
    }

    @Override
    public long getTotalCount() {
        return supplyRepository.count();
    }
}
