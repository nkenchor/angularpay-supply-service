package io.angularpay.supply.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.supply.adapters.outbound.MongoAdapter;
import io.angularpay.supply.domain.CommoditySupplier;
import io.angularpay.supply.domain.Role;
import io.angularpay.supply.domain.SupplyRequest;
import io.angularpay.supply.exceptions.ErrorObject;
import io.angularpay.supply.models.GetUserInvestmentsCommandRequest;
import io.angularpay.supply.models.UserInvestmentModel;
import io.angularpay.supply.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GetUserInvestmentsCommand extends AbstractCommand<GetUserInvestmentsCommandRequest, List<UserInvestmentModel>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetUserInvestmentsCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator) {
        super("GetUserInvestmentsCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetUserInvestmentsCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected List<UserInvestmentModel> handle(GetUserInvestmentsCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        List<UserInvestmentModel> investmentRequests = new ArrayList<>();
        List<SupplyRequest> response = this.mongoAdapter.listRequests(pageable).getContent();
        for (SupplyRequest supplyRequest : response) {
            List<CommoditySupplier> suppliers = supplyRequest.getSuppliers();
            for (CommoditySupplier supplier : suppliers) {
                if (request.getAuthenticatedUser().getUserReference().equalsIgnoreCase(supplier.getUserReference())) {
                    investmentRequests.add(UserInvestmentModel.builder()
                            .requestReference(supplyRequest.getReference())
                            .investmentReference(supplier.getReference())
                            .userReference(supplier.getUserReference())
                            .requestCreatedOn(supplier.getCreatedOn())
                            .build());
                }
            }
        }
        return investmentRequests;
    }

    @Override
    protected List<ErrorObject> validate(GetUserInvestmentsCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }
}
