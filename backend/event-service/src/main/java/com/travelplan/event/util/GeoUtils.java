package com.travelplan.event.util;

/**
 * EPIC-7: Pure-Java geospatial math utilities.
 *
 * Uses the Haversine formula for great-circle distances and an
 * equirectangular-projection approach for point-to-line-segment distance.
 * Accurate enough for Sri Lanka's geography (small country, low latitudes).
 */
public final class GeoUtils {

    public static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {}

    /**
     * Haversine great-circle distance between two lat/lng points (km).
     */
    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_KM * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * TASK-7.1: Perpendicular distance (km) from point P to the line segment A→B.
     *
     * Algorithm:
     *   1. Project everything onto a local 2-D equirectangular plane centred on A.
     *      x-axis is scaled by cos(midLat) to compensate longitude compression.
     *   2. Find the parameter t ∈ [0,1] of the closest point Q on the segment.
     *   3. Compute Haversine(P, Q) for the accurate metric result.
     *
     * @param pLat event latitude
     * @param pLng event longitude
     * @param aLat segment start latitude
     * @param aLng segment start longitude
     * @param bLat segment end latitude
     * @param bLng segment end longitude
     * @return distance in km from the event to the nearest point on the segment
     */
    public static double pointToSegmentKm(
            double pLat, double pLng,
            double aLat, double aLng,
            double bLat, double bLng) {

        // Longitude correction factor at the mid-latitude of the segment
        double midLat = (aLat + bLat) / 2.0;
        double cosLat = Math.cos(Math.toRadians(midLat));

        // Equirectangular 2-D vectors relative to A
        double bx = (bLng - aLng) * cosLat;
        double by = bLat - aLat;
        double px = (pLng - aLng) * cosLat;
        double py = pLat - aLat;

        double abLen2 = bx * bx + by * by;

        if (abLen2 < 1e-10) {
            // Degenerate segment (start == end): fall back to point distance
            return haversineKm(pLat, pLng, aLat, aLng);
        }

        // t is the projection of AP onto AB, clamped to [0,1]
        double t = Math.max(0.0, Math.min(1.0, (px * bx + py * by) / abLen2));

        // Closest point Q on the segment
        double closestLat = aLat + t * (bLat - aLat);
        double closestLng = aLng + t * (bLng - aLng);

        return haversineKm(pLat, pLng, closestLat, closestLng);
    }

    /**
     * Bounding-box padding in degrees for a given km radius.
     * Used to build the initial SQL pre-filter before the accurate Haversine check.
     * 1 degree of latitude ≈ 111.32 km everywhere; longitude varies but we use the
     * same value (conservative — slightly over-fetches, which is correct).
     */
    public static double kmToDegrees(double km) {
        return km / 111.32;
    }
}
