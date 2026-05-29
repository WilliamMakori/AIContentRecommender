package com.recommender.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recommender.model.Recommendation;
import com.recommender.repository.RecommendationRepository;
import com.recommender.repository.UserProfileRepository;
import com.recommender.service.BedrockService;
import com.recommender.service.RecommendationService;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.Map;

public class GetRecommendationsHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public GetRecommendationsHandler() {
        // Build the DynamoDB client
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();

        // Build the Bedrock client
        BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder().build();

        // Wire everything together
        UserProfileRepository userProfileRepository =
            new UserProfileRepository(dynamoDbClient);
        RecommendationRepository recommendationRepository =
            new RecommendationRepository(dynamoDbClient);
        BedrockService bedrockService =
            new BedrockService(bedrockClient);

        this.recommendationService = new RecommendationService(
            bedrockService,
            recommendationRepository,
            userProfileRepository
        );

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event,
            Context context) {

        context.getLogger().log("GetRecommendations called for: "
            + event.getPathParameters());

        try {
            // Step 1 — extract userId from the URL path
            String userId = event.getPathParameters().get("userId");

            if (userId == null || userId.isEmpty()) {
                return buildResponse(400, "{\"error\": \"userId is required\"}");
            }

            // Step 2 — get recommendations from the service
            Recommendation recommendation = recommendationService
                .getRecommendations(userId);

            // Step 3 — convert to JSON and return
            String responseBody = objectMapper.writeValueAsString(recommendation);
            return buildResponse(200, responseBody);

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return buildResponse(500,
                "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    // Helper method to build a standard HTTP response
    private APIGatewayProxyResponseEvent buildResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");

        return new APIGatewayProxyResponseEvent()
            .withStatusCode(statusCode)
            .withHeaders(headers)
            .withBody(body);
    }
}  