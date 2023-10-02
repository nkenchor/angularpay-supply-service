package io.angularpay.supply.models;

import io.angularpay.supply.domain.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthenticatedUser {
    private String username;
    private String userReference;
    private String deviceId;
    private String correlationId;
    private String clientIp;
    private List<Role> roles;
}
