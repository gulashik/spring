package ru.gulash.spring.repostory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gulash.spring.domain.User;
import ru.gulash.spring.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({UserService.class}) // todo добираем нужное через Import
@DataMongoTest // todo контекст связанный с MongoDB
//@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void findByName() {
        User expectedUser = userRepository.save(new User("xxx", 20));

        List<User> actualUser = userService.findUsersByName(expectedUser.getName());

        assertThat(actualUser).hasSize(1);
        assertThat(actualUser.get(0)).isEqualTo(expectedUser);
    }
}