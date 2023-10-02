package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.models.GenericGetRequestListCommandRequest;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GetRequestListCommand extends AbstractCommand<GenericGetRequestListCommandRequest, List<SupplyRequest>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetRequestListCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator) {
        super("GetRequestListCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GenericGetRequestListCommandRequest request) {
        return "";
    }

    @Override
    protected List<SupplyRequest> handle(GenericGetRequestListCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        return this.mongoAdapter.listRequests(pageable).getContent();
    }

    @Override
    protected List<ErrorObject> validate(GenericGetRequestListCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_KYC_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
