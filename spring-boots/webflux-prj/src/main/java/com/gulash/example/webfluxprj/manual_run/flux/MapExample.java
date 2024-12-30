package com.gulash.example.webfluxprj.manual_run.flux;

import com.gulash.example.webfluxprj.model.Notes;
import com.gulash.example.webfluxprj.model.PersonDto;
import com.gulash.example.webfluxprj.repository.NotesRepo;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
/*
            ПРОСТО ДЕМОНСТРАЦИЯ НЕ РАБОТАЕТ
*/

@RequiredArgsConstructor
@RestController
class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public Flux<UserDTO> getUsers() {
        return userService.getAllUsers()
            // todo MAP - СИНХРОННО; Преобразуем User в UserDTO
            .map(user -> new UserDTO(user.getId(), user.getName()))
            // todo FLATMAP - АСИНХРОННО; Обновляем или добавляем дополнительную информацию
            .flatMap(userDTO -> userService.enrichUser(userDTO));
    }
}

// ниже не смотрим ------------------------
@Service
class UserService {


    public Flux<User> getAllUsers() {
        return Flux.just(
            new User(1, "Alice"),
            new User(2, "Bob"),
            new User(3, "Charlie")
        );
    }
    public Mono<UserDTO> enrichUser(UserDTO userDTO) {
        // Эмуляция асинхронного вызова для обогащения пользователя
        return Mono.just(userDTO)
            .map(dto -> {
                dto.setExtraInfo("Extra information for user " + dto.getName());
                return dto;
            });
    }

}

@Data
@AllArgsConstructor
class User {
    private Integer id;
    private String name;
}
@Getter
@RequiredArgsConstructor
class UserDTO {
    private final Integer id;
    private final String name;
    @Setter
    private String extraInfo;
}