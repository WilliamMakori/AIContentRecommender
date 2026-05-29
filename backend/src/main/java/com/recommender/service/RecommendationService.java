package com.recommender.service;

import com.recommender.model.Recommendation;
import com.recommender.model.Recommendation.RecommendedItem;
import com.recommender.model.UserProfile;
import com.recommender.repository.RecommendationRepository;
import com.recommender.repository.UserProfileRepository;

import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    private final BedrockService bedrockService;
    private final RecommendationRepository recommendationRepository;
    private final UserProfileRepository userProfileRepository;

    public RecommendationService(
            BedrockService bedrockService,
            RecommendationRepository recommendationRepository,
            UserProfileRepository userProfileRepository) {
        this.bedrockService = bedrockService;
        this.recommendationRepository = recommendationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // Main method — gets recommendations for a user
    public Recommendation getRecommendations(String userId) throws Exception {

        // Step 1 — check if fresh recommendations already exist
        if (recommendationRepository.hasFreshRecommendation(userId)) {
            return recommendationRepository.findLatestByUserId(userId);
        }

        // Step 2 — no fresh recommendations, fetch the user's profile
        UserProfile profile = userProfileRepository.findById(userId);
        if (profile == null) {
            throw new Exception("User not found: " + userId);
        }

        // Step 3 — make sure the user has preferences set
        List<String> genres = profile.getPreferredGenres();
        List<String> watchHistory = profile.getWatchHistory();

        if (genres == null || genres.isEmpty()) {
            genres = getDefaultGenres();
        }

        if (watchHistory == null) {
            watchHistory = new ArrayList<>();
        }

        // Step 4 — call Bedrock to generate fresh recommendations
        List<RecommendedItem> items = bedrockService.generateRecommendations(
            userId,
            genres,
            watchHistory
        );

        // Step 5 — wrap the items in a Recommendation object and save it
        Recommendation recommendation = Recommendation.createNew(userId, items);
        recommendationRepository.save(recommendation);

        return recommendation;
    }

    // Update a user's preferred genres
    public void updatePreferences(String userId, List<String> newGenres) throws Exception {
        UserProfile profile = userProfileRepository.findById(userId);
        if (profile == null) {
            throw new Exception("User not found: " + userId);
        }

        profile.setPreferredGenres(newGenres);
        profile.setUpdatedAt(java.time.Instant.now().toString());
        userProfileRepository.save(profile);
    }

    // Add a title to a user's watch history
    public void addToWatchHistory(String userId, String contentTitle) throws Exception {
        UserProfile profile = userProfileRepository.findById(userId);
        if (profile == null) {
            throw new Exception("User not found: " + userId);
        }

        List<String> history = profile.getWatchHistory();
        if (history == null) {
            history = new ArrayList<>();
        }

        if (!history.contains(contentTitle)) {
            history.add(contentTitle);
            profile.setWatchHistory(history);
            profile.setUpdatedAt(java.time.Instant.now().toString());
            userProfileRepository.save(profile);
        }
    }

    // Create a brand new user profile
    public UserProfile createUser(String userId, String email, String displayName) {
        UserProfile profile = UserProfile.createNew(userId, email, displayName);
        userProfileRepository.save(profile);
        return profile;
    }

    // Default genres for new users who haven't set preferences yet
    private List<String> getDefaultGenres() {
        List<String> defaults = new ArrayList<>();
        defaults.add("Action");
        defaults.add("Drama");
        defaults.add("Comedy");
        return defaults;
    }
}