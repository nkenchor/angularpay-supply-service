package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.SupplyRequest;

import java.util.Objects;

public interface UpdatesPublisherCommand<T extends SupplyRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToUpdatesMessage(SupplyRequest supplyRequest) throws JsonProcessingException;

    default void publishUpdates(T t) {
        SupplyRequest supplyRequest = t.getSupplyRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(supplyRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToUpdatesMessage(supplyRequest);
                redisAdapter.publishUpdates(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
