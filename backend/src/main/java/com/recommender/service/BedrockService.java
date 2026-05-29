package com.recommender.service;

import com.recommender.model.Recommendation.RecommendedItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedrockService {

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;
    private static final String MODEL_ID = "amazon.titan-text-express-v1";

    public BedrockService(BedrockRuntimeClient bedrockClient) {
        this.bedrockClient = bedrockClient;
        this.objectMapper = new ObjectMapper();
    }

    // Build a prompt from the user's preferences and watch history
    private String buildPrompt(List<String> preferredGenres, List<String> watchHistory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a content recommendation engine. ");
        prompt.append("Based on the following user preferences, recommend exactly 5 content titles. ");
        prompt.append("For each recommendation provide: title, genre, reason, and a confidence score between 0 and 1. ");
        prompt.append("Respond in this exact JSON format: ");
        prompt.append("{\"recommendations\": [{\"title\": \"\", \"genre\": \"\", \"reason\": \"\", \"confidenceScore\": 0.0}]} ");
        prompt.append("User preferred genres: ");
        prompt.append(String.join(", ", preferredGenres));
        prompt.append(". Previously watched: ");
        prompt.append(String.join(", ", watchHistory));
        prompt.append(". Do not recommend anything already in the watched list.");
        return prompt.toString();
    }

    // Call Bedrock and get recommendations back
    public List<RecommendedItem> generateRecommendations(
            String userId,
            List<String> preferredGenres,
            List<String> watchHistory) throws Exception {

        // Build the prompt
        String prompt = buildPrompt(preferredGenres, watchHistory);

        // Wrap the prompt in the format Bedrock's Titan model expects
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("inputText", prompt);

        Map<String, Object> textConfig = new HashMap<>();
        textConfig.put("maxTokenCount", 1024);
        textConfig.put("temperature", 0.7);
        textConfig.put("topP", 0.9);
        requestMap.put("textGenerationConfig", textConfig);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        // Send the request to Bedrock
        InvokeModelRequest request = InvokeModelRequest.builder()
            .modelId(MODEL_ID)
            .contentType("application/json")
            .accept("application/json")
            .body(SdkBytes.fromUtf8String(requestBody))
            .build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);

        // Parse the response
        String responseBody = response.body().asUtf8String();
        JsonNode root = objectMapper.readTree(responseBody);
        String outputText = root.path("results").get(0).path("outputText").asText();

        // Extract the JSON from the output text
        JsonNode recommendationsNode = objectMapper.readTree(outputText)
            .path("recommendations");

        // Convert JSON into RecommendedItem objects
        List<RecommendedItem> items = new ArrayList<>();
        for (JsonNode node : recommendationsNode) {
            RecommendedItem item = new RecommendedItem();
            item.setTitle(node.path("title").asText());
            item.setGenre(node.path("genre").asText());
            item.setReason(node.path("reason").asText());
            item.setConfidenceScore(node.path("confidenceScore").asDouble());
            items.add(item);
        }

        return items;
    }
}