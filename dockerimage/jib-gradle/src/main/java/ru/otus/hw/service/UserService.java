package ru.otus.hw.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.entity.User;
import ru.otus.hw.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user with id: {}", id);
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        logger.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email);
    }

    public List<User> searchUsers(String search) {
        logger.info("Searching users with term: {}", search);
        if (search == null || search.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.findByNameOrEmailContaining(search.trim());
    }

    public User createUser(User user) {
        logger.info("Creating new user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    public User updateUser(Long id, User userDetails) {
        logger.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(userDetails.getEmail()) &&
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("User with email " + userDetails.getEmail() + " already exists");
        }

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully with id: {}", updatedUser.getId());
        return updatedUser;
    }

    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        logger.info("User deleted successfully with id: {}", id);
    }
}
