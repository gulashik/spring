package com.gulash.springmvc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Ответ по HTTP коду") // Сообщение указываем тут
// или
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public// Сообщение передадим при throw new UserException("Нужное сообщение");
class UserException
    extends RuntimeException // Наследуемся от RuntimeException
{
    public UserException(String message) { // В Конструкторе прокидываем сообщение
        super(message);
    }
}
