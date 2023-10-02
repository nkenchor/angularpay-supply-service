package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.SupplyRequest;

import java.util.Objects;

public interface TTLPublisherCommand<T extends SupplyRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToTTLMessage(SupplyRequest supplyRequest, T t) throws JsonProcessingException;

    default void publishTTL(T t) {
        SupplyRequest supplyRequest = t.getSupplyRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(supplyRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToTTLMessage(supplyRequest, t);
                redisAdapter.publishTTL(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
