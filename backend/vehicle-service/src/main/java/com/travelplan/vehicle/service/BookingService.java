package com.travelplan.vehicle.service;

import com.travelplan.vehicle.dto.BookingRequestDTO;
import com.travelplan.vehicle.entity.Booking;
import com.travelplan.vehicle.entity.Vehicle;
import com.travelplan.vehicle.repository.BookingRepository;
import com.travelplan.vehicle.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        // 1. Validate Date Range
        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // 2. Validate Vehicle Existence
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + request.vehicleId()));

        // 3. Check if vehicle is soft-deleted or inactive
        if (!Boolean.TRUE.equals(vehicle.getIsActive())) {
            throw new IllegalStateException("Vehicle is no longer available (removed from listings)");
        }

        // 4. Check for overlapping bookings (This causes the 400 Bad Request if dates
        // conflict)
        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
                request.vehicleId(),
                "CONFIRMED",
                request.startDate(),
                request.endDate());

        if (isOverlapping) {
            throw new IllegalStateException("Vehicle is not available for the selected dates");
        }

        // 5. Create Booking Entity
        Booking booking = new Booking();
        booking.setVehicleId(request.vehicleId());
        booking.setCustomerName(request.customerName());
        booking.setCustomerEmail(request.customerEmail());
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setStatus("CONFIRMED");

        // 6. Set Total Price (Use the one from frontend or recalculate for safety)
        if (request.totalPrice() != null) {
            booking.setTotalPrice(request.totalPrice());
        } else {
            long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
            booking.setTotalPrice(vehicle.getDailyRate().multiply(java.math.BigDecimal.valueOf(Math.max(1, days))));
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByCustomer(String email) {
        return bookingRepository.findByCustomerEmail(email);
    }

    @Transactional
    public Booking completeBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if ("COMPLETED".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking is already completed");
        }

        booking.setStatus("COMPLETED");
        return bookingRepository.save(booking);
    }
}