package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Location;
import entity.Metadata;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DataProcessor {
    DecimalFormat df = new DecimalFormat("#.##");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void processData(String locationFileName, String metadataFileName) {
        try {
            // Step 1: Load JSON data
            List<Location> locations = loadLocations(locationFileName);
            List<Metadata> metadataList = loadMetadata(metadataFileName);

            // Step 2: Convert metadata list to a Map for fast lookup
            Map<String, Metadata> metadataMap = metadataList.stream()
                    .collect(Collectors.toMap(m -> m.id, m -> m));

            // Step 3: Compute required statistics
            processStatistics(locations, metadataMap);
        } catch (IOException e) {
            System.err.println("Error loading JSON files: " + e.getMessage());
        }
    }

    private List<Location> loadLocations(String fileName) throws IOException {
        return objectMapper.readValue(new File(fileName), new TypeReference<>() {});
    }

    private List<Metadata> loadMetadata(String fileName) throws IOException {
        return objectMapper.readValue(new File(fileName), new TypeReference<>() {});
    }

    private void processStatistics(List<Location> locations, Map<String, Metadata> metadataMap) {
        Map<String, Long> typeCount = new HashMap<>();
        Map<String, Double> totalRating = new HashMap<>();
        Map<String, Integer> ratingCount = new HashMap<>();
        String maxReviewedLocation = null;
        int maxReviews = 0;
        List<String> incompleteDataLocations = new ArrayList<>();

        for (Location loc : locations) {
            Metadata meta = metadataMap.get(loc.id);
            if (meta != null) {
                // Update type count
                typeCount.merge(meta.type, 1L, Long::sum);

                // Update ratings for average calculation
                if (meta.rating != null) {
                    totalRating.merge(meta.type, meta.rating, Double::sum);
                    ratingCount.merge(meta.type, 1, Integer::sum);
                }

                // Find the location with the highest reviews
                if (meta.reviews != null && meta.reviews > maxReviews) {
                    maxReviewedLocation = loc.id;
                    maxReviews = meta.reviews;
                }
            } else {
                incompleteDataLocations.add(loc.id);
            }
        }

        // Step 4: Compute average ratings
        Map<String, Double> avgRating = calculateAverageRatings(totalRating, ratingCount);

        // Step 5: Display results
        displayResults(typeCount, avgRating, maxReviewedLocation, maxReviews, incompleteDataLocations);
    }

    private Map<String, Double> calculateAverageRatings(Map<String, Double> totalRating, Map<String, Integer> ratingCount) {
        DecimalFormat df = new DecimalFormat("#.##"); // Formats to 2 decimal places

        return totalRating.entrySet().stream()
                .filter(entry -> ratingCount.containsKey(entry.getKey()) && ratingCount.get(entry.getKey()) > 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Double.parseDouble(df.format(entry.getValue() / ratingCount.get(entry.getKey())))
                ));
    }

    private void displayResults(Map<String, Long> typeCount, Map<String, Double> avgRating,
                                String maxReviewedLocation, int maxReviews, List<String> incompleteDataLocations) {
        System.out.println("Valid Points Per Type: " + typeCount);
        System.out.println("Average Rating Per Type: " + avgRating);

        if (maxReviewedLocation != null) {
            System.out.println("Location with highest reviews: " + maxReviewedLocation + " (" + maxReviews + " reviews)");
        }

        if (!incompleteDataLocations.isEmpty()) {
            System.out.println("Locations with incomplete data: " + incompleteDataLocations);
        }
    }
}
