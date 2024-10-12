package com.gulash.springmvc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice // Содержит методы с @ExceptionHandler(НужныйКласс.class)
// Перехватывает ошибки ВСЕХ Controller-ов приложения по классу из ExceptionHandler сейчас это "FlightExceptionExternal.class"
public class FlightControllerExceptionHandlerCustomExceptions {

    @ExceptionHandler(FlightExceptionExternal.class)
    public ResponseEntity<FlightCustomErrorMessage> handleFlightNotFound(FlightExceptionExternal e, WebRequest request)
    {
        FlightCustomErrorMessage body = new FlightCustomErrorMessage(
                                                                    HttpStatus.NOT_FOUND.value(),
                                                                    LocalDateTime.now(),
                                                                    e.getMessage() + " by @ControllerAdvice from FlightControllerExceptionHandlerCustomExceptions",
                                                                    request.getDescription(false)
                                                                    );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}