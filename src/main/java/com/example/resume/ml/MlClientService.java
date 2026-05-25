package com.example.resume.ml;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MlClientService {
    private final RestClient restClient;
    private final String mlServiceUrl;

    public MlClientService(
            RestClient.Builder restClientBuilder,
            @Value("${ml.service.url}") String mlServiceUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.mlServiceUrl = mlServiceUrl;
    }

    public MlPredictionResponse predict(String resumeText, String jobDescription) {
        MlPredictionRequest request = new MlPredictionRequest(
                resumeText,
                jobDescription
        );

        return restClient.post()
                .uri(mlServiceUrl + "/predict")
                .body(request)
                .retrieve()
                .body(MlPredictionResponse.class);
    }
}
