package io.angularpay.supply.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}
