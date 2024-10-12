package com.gulash.springmvc.exception;

// Пользовательское исключение будет выброшено throw new FlightNotFoundExceptionExternal(...)
public class FlightExceptionExternal extends RuntimeException {
    public FlightExceptionExternal(String message) {
        super(message);
    }
}
