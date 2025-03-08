package ru.otus.hw.exception.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.otus.hw.exception.EntityNotFoundException;

@Slf4j
@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFound(EntityNotFoundException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Не найдено! " + exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> responseNotFound(RuntimeException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Server error! The request could not be completed."));
    }

}
