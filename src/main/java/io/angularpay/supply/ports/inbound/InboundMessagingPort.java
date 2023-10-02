package io.angularpay.supply.ports.inbound;

import io.angularpay.supply.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
