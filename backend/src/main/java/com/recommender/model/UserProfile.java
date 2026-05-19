package com.recommender.model;
// this acts like a home address for this file, tells java that the file lives in the model folder
// inside the recommender project. 
// Every Java file starts by declaring where it lives, without this Java can't find the file when other parts of your code need it

import java.util.List; // the user profile stores a list of genres they like
// eg action, sci-fi and thriller stored in the form of strings in an [] array
// java doesn't know what a list is by default so the line imports from Java's built in library
// grab the List tool from the toolbox 
// same is done in the map line below
// map stores pairs of things, each genre is stored with a score, stores how much a user likes each genre

import java.util.Map; 

public class UserProfile { 
    // class means blueprint in Java and public means other parts of your code can access it
    // everything in the brackets belong to this blueprint
    // the private keyword here is used for encapsulation, in this case it means that only parts of code within this class can access it and other parts of your code need special methods/ functions to access it

    private String userId; // their unique userID that's used to identify them, each user is assigned one 
    private String email; // their registered email that they use to login 
    private String displayName; // the user's display name
    private List<String> preferredGenres; // list of genres the user likes
    private List<String> watchHistory; // things the user has watched before
    private Map<String,Integer> genreScores; // stores how much the user likes each Genre, probably takes the genres from the List that stores each Genre above
    private String createdAt; // when the user's profile was created
    private String updatedAt; // last time the profile was updated at
    // the last two variables are timestamps they're stored in ISO format 

    // very similar to C++, we now add the member methods for the class
    // Getters and setters
    // this is the for the userID and the same thing is done for the other variables
    // another word for variable is property

    public String getUserId() {return userId; }
    public void setUserId(String userID) {this.userId = userID;}

    // email
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    //displayName and etc 
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public List<String> getPreferredGenres() { return preferredGenres; }
    public void setPreferredGenres(List<String> preferredGenres) { this.preferredGenres = preferredGenres; }

    public List<String> getWatchHistory() { return watchHistory; }
    public void setWatchHistory(List<String> watchHistory) { this.watchHistory = watchHistory; }

    public Map<String, Integer> getGenreScores() { return genreScores; }
    public void setGenreScores(Map<String, Integer> genreScores) { this.genreScores = genreScores; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }


    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Factory method in Java 
    public static UserProfile createNew(String userId, String email, String displayName) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setEmail(email);
        profile.setDisplayName(displayName);
        profile.setCreatedAt(java.time.Instant.now().toString());
        profile.setUpdatedAt(java.time.Instant.now().toString());
        return profile;
    }
}