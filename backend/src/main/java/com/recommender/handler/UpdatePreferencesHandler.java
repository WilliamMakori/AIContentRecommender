package com.recommender.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recommender.repository.RecommendationRepository;
import com.recommender.repository.UserProfileRepository;
import com.recommender.service.BedrockService;
import com.recommender.service.RecommendationService;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePreferencesHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public UpdatePreferencesHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
        BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder().build();

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

        context.getLogger().log("UpdatePreferences called");

        try {
            // Step 1 — extract userId from the URL path
            String userId = event.getPathParameters().get("userId");
            if (userId == null || userId.isEmpty()) {
                return buildResponse(400, "{\"error\": \"userId is required\"}");
            }

            // Step 2 — parse the request body to get the new genres
            String body = event.getBody();
            if (body == null || body.isEmpty()) {
                return buildResponse(400, "{\"error\": \"Request body is required\"}");
            }

            JsonNode bodyNode = objectMapper.readTree(body);
            JsonNode genresNode = bodyNode.get("genres");

            if (genresNode == null || !genresNode.isArray()) {
                return buildResponse(400, "{\"error\": \"genres array is required\"}");
            }

            // Step 3 — convert the JSON array into a Java List
            List<String> newGenres = new ArrayList<>();
            for (JsonNode genre : genresNode) {
                newGenres.add(genre.asText());
            }

            if (newGenres.isEmpty()) {
                return buildResponse(400, "{\"error\": \"genres cannot be empty\"}");
            }

            // Step 4 — update the user's preferences
            recommendationService.updatePreferences(userId, newGenres);

            return buildResponse(200,
                "{\"message\": \"Preferences updated successfully\"}");

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return buildResponse(500,
                "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

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
