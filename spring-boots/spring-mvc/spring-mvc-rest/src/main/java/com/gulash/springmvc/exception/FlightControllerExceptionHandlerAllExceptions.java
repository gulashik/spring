package com.gulash.springmvc.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// @ControllerAdvice - класс отлавливает исключения по всему приложению ...
//  ПОЛЬЗОВАТЕЛЬСКИЕ исключения через @ExceptionHandler(НужныйКласс.class)
//  СТАНДАРТНЫЕ исключения для Spring MVC шаблонов через наследование ResponseEntityExceptionHandler + @Override нужных методов
@ControllerAdvice
public class FlightControllerExceptionHandlerAllExceptions
        extends ResponseEntityExceptionHandler
{
    // Переопределяем НУЖНЫЕ методы для перехвата СТАНДАРТНЫХ ИСКЛЮЧЕНИЙ
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        // Наполняем body ошибки
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("timestamp", LocalDateTime.now());
        body.put("exception", ex.getClass());
        body.put("message", "StandartExcept from class FlightControllerExceptionHandlerStandartExcept");

        // Возвращаем Ошибку через ResponseEntity
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(FlightExceptionExternal2.class)
    public ResponseEntity<FlightCustomErrorMessage> handleFlightNotFound(FlightExceptionExternal2 e, WebRequest request)
    {
        // Класс содержит формирование BODY ошибки
        FlightCustomErrorMessage body = new FlightCustomErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                e.getMessage() + " by @ControllerAdvice from class FlightControllerExceptionHandlerStandartExcept",
                request.getDescription(false)
        );
        // Возвращаем Ошибку через ResponseEntity
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}

