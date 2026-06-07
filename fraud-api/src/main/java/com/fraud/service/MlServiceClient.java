package com.fraud.service;

import com.fraud.dto.PredictionResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class MlServiceClient {

    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public MlServiceClient(
            @Value("${ml-service.url}") String mlServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.mlServiceUrl = mlServiceUrl;
    }

    public PredictionResult predict(BigDecimal amount){
        final var url = mlServiceUrl + "/predict";
        Map<String,Object> request = new HashMap<>();
        request.put("amount", amount.doubleValue());
        request.put("time", 0);
        request.put("v_features", Collections.nCopies(28, 0));

        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response == null) {
            throw new RuntimeException("ML service returned null response");
        }

        final var fraud = Boolean.parseBoolean(response.get("fraud").toString());

        final var confidence = Double.parseDouble(response.get("confidence").toString());

        return new PredictionResult(fraud, confidence);


    }


}
