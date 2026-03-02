package com.travelplan.itinerary.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Utility class for generating GeoJSON structures for map visualization
 */
public class GeoJsonUtil {

    /**
     * Creates a GeoJSON Feature Collection from a list of points
     */
    public static Map<String, Object> createFeatureCollection(List<GeoJsonFeature> features) {
        Map<String, Object> featureCollection = new LinkedHashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", features);
        return featureCollection;
    }

    /**
     * Creates a GeoJSON Feature for a point
     */
    public static GeoJsonFeature createPointFeature(double latitude, double longitude, Map<String, Object> properties) {
        return GeoJsonFeature.builder()
                .type("Feature")
                .geometry(createPointGeometry(latitude, longitude))
                .properties(properties)
                .build();
    }

    /**
     * Creates a GeoJSON LineString geometry for route visualization
     */
    public static Map<String, Object> createLineStringGeometry(List<double[]> coordinates) {
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);
        return geometry;
    }

    /**
     * Creates a GeoJSON Point geometry
     */
    public static Map<String, Object> createPointGeometry(double latitude, double longitude) {
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "Point");
        geometry.put("coordinates", new double[]{longitude, latitude});
        return geometry;
    }

    /**
     * Creates a route LineString Feature
     */
    public static GeoJsonFeature createRouteFeature(List<double[]> coordinates, String dayColor, Map<String, Object> properties) {
        return GeoJsonFeature.builder()
                .type("Feature")
                .geometry(createLineStringGeometry(coordinates))
                .properties(properties)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeoJsonFeature {
        private String type;
        private Map<String, Object> geometry;
        private Map<String, Object> properties;
    }
}
