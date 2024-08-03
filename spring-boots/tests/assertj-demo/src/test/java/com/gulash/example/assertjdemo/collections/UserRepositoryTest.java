package com.gulash.example.assertjdemo.collections;

import com.gulash.example.assertjdemo.entity.User;
import com.gulash.example.assertjdemo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind() {
        User user = new User("John");
        userRepository.save(user);

        List<User> users = userRepository.findAll();
        assertThat(users)
            .isNotEmpty()
            .hasSize(1)
            // todo extracting аналог map
            //.extracting(User::getName)
            // или
            .extracting( curUser  -> {
                System.out.println("checked user is " + curUser);
                // todo действия внутри
                if(curUser.getId() > 100) {
                    throw new AssertionError("Too big id -" + curUser.getId());
                }
                return curUser.getName();
            })
            .extracting( str-> {
                System.out.println(str);
                return str;
            })
            // todo действия после - действие над результатом
            .contains("John");
    }
}