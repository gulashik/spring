package com.gulash.springmvc.exception;

import java.time.LocalDateTime;


// Класс для создания BODY ошибки
public class FlightCustomErrorMessage {
    private int statusCode;
    private LocalDateTime timestamp;
    private String message;
    private String description;

    public FlightCustomErrorMessage(
                                    int statusCode,
                                    LocalDateTime timestamp,
                                    String message,
                                    String description
                                    )
    {

        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
