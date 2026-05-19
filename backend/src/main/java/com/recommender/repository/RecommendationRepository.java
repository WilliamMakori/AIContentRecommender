package com.recommender.repository; 
// where the file is 

import com.recommender.model.Recommendation; 
import com.recommender.model.Recommendation.RecommendedItem; 

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*; 

import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List;
import java.util.Map; 
