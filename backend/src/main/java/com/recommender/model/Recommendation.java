package com.recommender.model;

import java.util.List;

// these models are needed to outline how data is stored in the database
public class Recommendation {


    private String userId; //the recommendation needs to be mapped to a specific user
    private String generatedAt;// just stores when the recommendation was generated
    private String expiresAt; // 24 hours after it was generated
    // so its from the time that the recommendation was created + the number of seconds in one day
    // java.time.Instant.now().plusSeconds(number of seconds in the time lapse)
    // which library does this function come from, comes from the jave general library, the time function and instant now comes from 
    // will expired recommendations be deleted from the database? 
    private List<RecommendedItem> items; 
    // it's not just one thing, it's a list of suggested content items
    // title, genre, reason, confidenceScore, these items have to be stored in an List with each one having its own specifications
    // there won't be just one recommendation and all of the ones that exist will be stored in here
    private String modelUsed; // records wich Bedrock AI model generated these recommendations, we're using Amazon's Titan model, storing this 
    // is good practice because AWS has many models and we might want to change it later, helps in comparing quality 

    // Getters and Setters, the setters are used to modify instances of the class that have already been created and are used in the factory method
    
    public String getUserId() { return userId; } 
    public void setUserId(String userId) { this.userId = userId; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public List<RecommendedItem> getItems() { return items; }
    public void setItems(List<RecommendedItem> items) { this.items = items; }
    // there will be a list of possible models to be used to generate responses

    // can be used to make comparisons later on
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    // recommendation will have a list of contents that describe what it is and why it was suggested, and also how much confidence has in the accuracy of the recommendation on a scale of 1-10

    // Factory method, constructor
    public static Recommendation createNew(String userId, List<RecommendedItem> items) {
        Recommendation rec = new Recommendation();
        rec.setUserId(userId);
        rec.setItems(items);
        rec.setGeneratedAt(java.time.Instant.now().toString());
        rec.setExpiresAt(java.time.Instant.now().plusSeconds(86400).toString());
        rec.setModelUsed("amazon.titan-text-express-v1");
        return rec;
    }

    // Inner class, contents of the recommendation, title genre reason etc
    public static class RecommendedItem {

        private String title;
        private String genre;
        private String reason;
        private double confidenceScore;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }
}