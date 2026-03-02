package com.travelplan.vehicle.controller;

import com.travelplan.vehicle.dto.BookingRequestDTO;
import com.travelplan.vehicle.entity.Booking;
import com.travelplan.vehicle.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // 1. CREATE BOOKING
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO bookingRequest) {
        try {
            Booking booking = bookingService.createBooking(bookingRequest);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Server Error: " + e.getMessage());
        }
    }

    // 2. GET ALL BOOKINGS
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // 3. COMPLETE BOOKING (This fixes the 404 Error!)
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingService.completeBooking(id);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            // Returns 404 ONLY if the booking ID doesn't exist in the database
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}