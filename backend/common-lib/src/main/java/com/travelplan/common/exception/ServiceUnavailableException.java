package com.travelplan.common.exception;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(String.format("Service '%s' is currently unavailable", serviceName), cause);
    }
}
