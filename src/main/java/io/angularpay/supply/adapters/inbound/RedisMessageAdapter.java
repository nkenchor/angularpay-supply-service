package io.angularpay.supply.adapters.inbound;

import io.angularpay.supply.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.supply.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.supply.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.supply.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
