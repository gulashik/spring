package com.gulash.springmvc.exception;

// Пользовательское исключение будет выброшено throw new FlightNotFoundExceptionExternal(...)
public class FlightExceptionExternal2 extends RuntimeException {
    public FlightExceptionExternal2(String message) {
        super(message);
    }
}
