package io.angularpay.supply.ports.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.supply.models.VerifySignatureResponseModel;

import java.util.Map;

public interface CipherServicePort {
    VerifySignatureResponseModel verifySignature(String requestBody, Map<String, String> headers) throws JsonProcessingException;
}
