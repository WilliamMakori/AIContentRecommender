// talks directly to DynamoDB, the database
package com.recommender.repository; 

// we'll use the class from the userprofile model 

import com.recommended.UserProfile; 
// not sure what the rest of this is or how it's used
// object mpper is used to map java objects to 

import com.fasterxml.jackson.databind.ObjectMapper; 
import software.amazon.awssdk.services.dynamodb.DynamoDbClient; 
import software.amazon.awssdk.services.dynamodb.model.*; 
// what is a dynamodb client, what is a dynamodb model and why does the statement end with a .*
import java.util.HashMap; 
import java.util.Map; 

public class UserProfileRepository{
    // final variables are 
    private final DynamoDbClient dynamoDbClient; 
    // what is a client in a dynamodb client
    private final ObjectMapper objectMapper; 
    private static final String TABLE_NAME = "user-profiles";

    public UserProfileRepository(DynamoDbClient dynamoDbClient){
        this.dynamoDbClient = dynamoDbClient; 
        this.objectMapper = new ObjectMapper(); 
    }

    // save user profile to DynamoDB
    // requirements? what exactly do we want the database to store and why, this is how we answer this question. 

    // functionality to add to an already existing value into the DynamoDB table, we can use the update item function, but for now we will just use the put item function which will overwrite the existing value with the new value, this is simpler and we can add the update functionality later on if we have time
    // here we're saving profiles to the database, if a value with the same userId already exists in the database, it gets overwritten with the new value if it doesn't exist then the new value is inserted

    public void save(UserProfile profile){
        // takes in a user object and saves it to the database, we need to convert the user profile object to a format that DynamoDB can store, which is a map of strings to attribute values, we can use the object mapper to convert the user profile object to a map of strings and attribute values 
        // lets study how hashmaps are used again, its similar to two dimensional array 

        Map<String,AttributeValue> item = new HashMap<>(); 
        // we have a map that stores strings and attributes. what does each variable represent? 
        // the map data structure has a put method that stores values to a  predefined key, in this case the key is the name of the variable in the database and the value is the value of that variable for that specific user profile
        // how does attribute builder work, it takes the specific variable from the profile instance and builds an attribute value, what data type is the attribute value in?

        item.put("userId", AttributeValue.builder().s(profile.getUserId()).build());
        item.put("email", AttributeValue.builder().s(profile.getEmail()).build());
        item.put("displayName", AttributeValue.builder().s(profile.getDisplayName()).build());
        item.put("createdAt", AttributeValue.builder().s(profile.getCreatedAt()).build());
        item.put("updatedAt", AttributeValue.builder().s(profile.getUpdatedAt()).build());

        // the values above can't be null, only preferred genres can be null so we add an extra layer or protection for it
        // why didn't we do this when we created the class? 

        
        // for the list of genres, we need to convert it to a format that DynamoDB can store, which is a list of strings
        // we   can use the ss method of the AttributeValue builder to convert a list of strings to a format that DynamoDB can store, but we also need to check if the list is not null before we do that, otherwise we might get a null pointer exception
          
        if (profile.getPreferredGenres() != null) {
            item.put("preferredGenres", AttributeValue.builder()
            .ss(profile.getPreferredGenres())
            .build());
        }

        // Put the item into the DynamoDB table
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }
    // Get a user profile from DynamoDB by userId

    public UserProfile findById(String userId){
        // we create a new hashMap
        Map<String,AttributeValue> key = new HashMap<>(); 
        key.put("userId", AttributeValue.builder().s(userId).build());

        GetItemRequest request = GetItemRequest.builder().tableName(TABLE_NAME).key(key).build();

        GetItemResponse response = dynamoDBClient.getItem(request); 

        if(!response.hasItem()){
            return null;
        }

        // what's happening here
        Map<String, AttributeValue> item = response.item();
        UserProfile profile = new UserProfile(); 
        profile.setUserId(item.get("userId").s()); 
        profile.setEmail(item.get("email").s()); 
        profile.setDisplayName(item.get("displayName").s()); 
        profile.setCreatedAt(item.get("createdAt").s());
        profile.setUpdatedAt(item.get("updatedAt").s()); 

        if(item.containsKey("preferredGenres")) {
            profile.setPreferredGenres(item.get("preferredGenres").ss());

        }

        return profile;
    }

    // deleting a user profile from DynamoDB
    public void delete(String userId){
        // we create a new HashMap variable
        // why do we need this? we use the id as the key in the key value pair and the rest of the contents of the 
        // and we pick a attributevalue to use to store the rest
        // use the created variable to search for and remove the value from the Database once we find it

        Map<String,AttributeValue> key = new HashMap<>();
        key.put("userId", AttributeValue.builder().s(userId).build()); 
        
        DeleteItemRequest request = DeleteItemRequest.builder().tableName(TABLE_NAME).key(key).build();

        dynamoDbClient.deleteItem(request); 

    
    }
}