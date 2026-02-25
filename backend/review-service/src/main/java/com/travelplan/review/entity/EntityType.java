package com.travelplan.review.entity;

/**
 * Represents the type of provider entity being reviewed.
 * Used to discriminate between hotels, tour guides, and vehicles
 * in the polymorphic reviews table.
 */
public enum EntityType {

    /** A hotel listing */
    HOTEL,

    /** A tour-guide profile */
    TOUR_GUIDE,

    /** A vehicle listing */
    VEHICLE
}
