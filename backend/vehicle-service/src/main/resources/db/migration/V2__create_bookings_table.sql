CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE CASCADE
);

-- Create an index on vehicle_id for faster lookups
CREATE INDEX idx_bookings_vehicle_id ON bookings(vehicle_id);

-- Create an index on customer_email for faster lookups
CREATE INDEX idx_bookings_customer_email ON bookings(customer_email);
