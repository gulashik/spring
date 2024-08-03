package com.gulash.springmvc.controller;

import com.gulash.springmvc.domain.FlightInfo;
import com.gulash.springmvc.exception.FlightCustomErrorMessage;
import com.gulash.springmvc.exception.FlightExceptionCommon;
import com.gulash.springmvc.exception.FlightExceptionExternal;
import com.gulash.springmvc.exception.FlightExceptionExternal2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// Класс REST-controller + Выбрасывание ошибки FlightExceptionCommon + Метод с @ExceptionHandler
@RestController
public class FlightController {

    private final List<FlightInfo> flightInfoList = Collections.synchronizedList(new ArrayList<>());

    public FlightController() {
        flightInfoList.add(
                new FlightInfo(
                        1,
                        "Delhi Indira Gandhi",
                        "Stuttgart",
                        "D80"));
        flightInfoList.add(
                new FlightInfo(
                        2,
                        "Tokyo Haneda",
                        "Frankfurt",
                        "110"));
    }

    // Указывает, что URL путь будет ассоциирован с GET запросом
    //  Шаблон - <api-path>/{param}
    // Пример GET http://localhost:7070/layerapp/flights/ЧИСЛО
    @GetMapping("flights/{id}")
    public FlightInfo getFlightInfo(@PathVariable long id) {
        for (var flightInfo : flightInfoList) {
            if (flightInfo.getId() == id) {
                return flightInfo;
            }
        }

        if(id == 11) {
            // Бросаем ошибку с FlightExceptionCommon - она будет перехвачена текущим классом методом с @ExceptionHandler(FlightExceptionCommon.class)
            throw new FlightExceptionCommon("Flight info not found id=" + id);
        } else if (id == 111){
            // Бросаем ошибку с FlightExceptionExternal - она будет перехвачена внешним классом FlightControllerExceptionHandlerCustomExceptions с @ControllerAdvice
            throw new FlightExceptionExternal("Flight info not found id=" + id);
        } else {
            // Бросаем ошибку с FlightExceptionExternal2 - она будет перехвачена внешним классом FlightControllerExceptionHandlerAllExceptions с @ControllerAdvice
            throw new FlightExceptionExternal2("Flight info not found id=" + id);
        }
    }
    // Пример GET http://localhost:7070/layerapp/flights
    // Получаем список всех полётов
    @GetMapping("/flights")
    public List<FlightInfo> getFlightsOne() {
        return flightInfoList;
    }

    // Пример GET http://localhost:7070/layerapp/flights
    // Получаем список всех полётов
    @GetMapping("/flights2")
    public List<FlightInfo> getFlightsTwo() {
        return flightInfoList;
    }

    // Указывает, что URL путь будет ассоциирован с POST запросом
    // POST http://localhost:7070/layerapp/flights/new
    // {
    //    "id": 9,
    //    "from": "Xxxx",
    //    "to": "Yyyy",
    //    "gate": "D80"
    // }
    // Обязательное использование потокобезопасных(thread-safe) объектов, чтобы избежать неожиданных изменений.
    @PostMapping("/flights/new")
    public void addNewFlightInfo(@RequestBody/*связывает Аргумет с BODY запроса*/ FlightInfo/*Преобразованный JSON объект*/ flightInfo) {
        flightInfoList.add(flightInfo);
    }

    // Перехватывает ошибки по классу из ExceptionHandler сейчас это "FlightExceptionCommon.class"
    // Только из этого контроллера
    @ExceptionHandler(FlightExceptionCommon.class) /*Класс будет throw new FlightExceptionCommon(...)*/
    public ResponseEntity</*Класс body ошибки*/FlightCustomErrorMessage> handleFlightNotFound(
                                                                                                FlightExceptionCommon e,
                                                                                                WebRequest request
                                                                                              )
    {
        // Класс содержит формирование BODY ошибки
        FlightCustomErrorMessage body = new FlightCustomErrorMessage(
                                                                    HttpStatus.NOT_FOUND.value(),
                                                                    LocalDateTime.now(),
                                                                    e.getMessage() + " by inner Controller @ExceptionHandler",
                                                                    request.getDescription(false)
                                                                    );
        // Возвращаем Ошибку через ResponseEntity
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}