package com.gulash.springmvc.exception;

// Пользовательское исключение будет выброшено throw new FlightNotFoundException(...)
public class FlightExceptionCommon extends RuntimeException {
    public FlightExceptionCommon(String message) {
        super(message);
    }
}
