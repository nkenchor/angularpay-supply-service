package io.angularpay.supply.ports.outbound;

import io.angularpay.supply.models.SchedulerServiceRequest;
import io.angularpay.supply.models.SchedulerServiceResponse;

import java.util.Map;
import java.util.Optional;

public interface SchedulerServicePort {
    Optional<SchedulerServiceResponse> createScheduledRequest(SchedulerServiceRequest request, Map<String, String> headers);
}
