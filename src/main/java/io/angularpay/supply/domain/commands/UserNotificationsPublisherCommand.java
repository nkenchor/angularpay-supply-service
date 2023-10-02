package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.supply.adapters.outbound.RedisAdapter;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.models.UserNotificationBuilderParameters;
import io.angularpay.supply.models.UserNotificationType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public interface UserNotificationsPublisherCommand<T extends SupplyRequestSupplier> {

    RedisAdapter getRedisAdapter();
    UserNotificationType getUserNotificationType(T commandResponse);
    List<String> getAudience(T commandResponse);
    String convertToUserNotificationsMessage(UserNotificationBuilderParameters<T, SupplyRequest> parameters) throws JsonProcessingException;

    default void publishUserNotification(T commandResponse) {
        SupplyRequest request = commandResponse.getSupplyRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        UserNotificationType type = this.getUserNotificationType(commandResponse);
        List<String> audience = this.getAudience(commandResponse);

        if (Objects.nonNull(request) && Objects.nonNull(redisAdapter)
        && Objects.nonNull(type) && !CollectionUtils.isEmpty(audience)) {
            audience.stream().parallel().forEach(userReference-> {
                try {
                    UserNotificationBuilderParameters<T, SupplyRequest> parameters = UserNotificationBuilderParameters.<T, SupplyRequest>builder()
                            .userReference(userReference)
                            .request(request)
                            .commandResponse(commandResponse)
                            .type(type)
                            .build();
                    String message = this.convertToUserNotificationsMessage(parameters);
                    redisAdapter.publishUserNotification(message);
                } catch (JsonProcessingException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }
}
