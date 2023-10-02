package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.models.GenericGetByStatusCommandRequest;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GetRequestListByStatusCommand extends AbstractCommand<GenericGetByStatusCommandRequest, List<SupplyRequest>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetRequestListByStatusCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator) {
        super("GetRequestListByStatusCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GenericGetByStatusCommandRequest request) {
        return "";
    }

    @Override
    protected List<SupplyRequest> handle(GenericGetByStatusCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        return this.mongoAdapter.findRequestsByStatus(pageable, request.getStatuses()).getContent();
    }

    @Override
    protected List<ErrorObject> validate(GenericGetByStatusCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_KYC_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
