package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import entity.Location;
import entity.MergedData;
import entity.Metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcessor {

    public void Process(String locationFileName, String metadataFileName) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        // Load JSON data
        List<Location> locations = objectMapper.readValue(new File(locationFileName), new TypeReference<>() {});
        List<Metadata> metadataList = objectMapper.readValue(new File(metadataFileName), new TypeReference<>() {});

        // Convert metadata list to a map for efficient lookup
        Map<String, Metadata> metadataMap = metadataList.stream().collect(Collectors.toMap(m -> m.id, m -> m));

        // Processing and aggregation in a single loop
        Map<String, Long> typeCount = new HashMap<>();
        Map<String, Double> totalRating = new HashMap<>();
        Map<String, Integer> ratingCount = new HashMap<>();
        MergedData maxReviewedLocation = null;
        int maxReviews = 0;
        List<String> incompleteDataLocations = new ArrayList<>();

        for (Location loc : locations) {
            Metadata meta = metadataMap.get(loc.id);
            if (meta != null) {
                // Update count of each type
                typeCount.merge(meta.type, 1L, Long::sum);

                // Sum ratings & count valid ratings for averaging
                if (meta.rating != null) {
                    totalRating.merge(meta.type, meta.rating, Double::sum);
                    ratingCount.merge(meta.type, 1, Integer::sum);
                }

                // Find location with highest reviews
                if (meta.reviews != null && meta.reviews > maxReviews) {
                    maxReviewedLocation = new MergedData(loc, meta);
                    maxReviews = meta.reviews;
                }
            } else {
                incompleteDataLocations.add(loc.id);
            }
        }

        // Compute average ratings
        Map<String, Double> avgRating = totalRating.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / ratingCount.get(e.getKey())));


        // Print results
        System.out.println("Valid Points Per Type: \n" + typeCount);
        System.out.println("\n\nAverage Rating Per Type: \n" + avgRating);
        if (maxReviewedLocation != null) {
            System.out.println("\n\nLocation with highest reviews: " + maxReviewedLocation.id + " (" + maxReviewedLocation.reviews + " reviews)");
        }
        if (!incompleteDataLocations.isEmpty()) {
            System.out.println("\n\nLocations with incomplete data: " + incompleteDataLocations);
        }


    }


}
