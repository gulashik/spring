package ru.gulash.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gulash.spring.domain.Post;
import ru.gulash.spring.domain.User;
import ru.gulash.spring.repostory.PostRepository;
import ru.gulash.spring.repostory.UserRepository;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public void createPost() {
        User user = userRepository.save(new User("John Doe", 20));
        Post post = new Post("My first post", user);
        postRepository.save(post);
    }

    public Post findPost(String template) {
        return postRepository.findByContentRegex(template);
    }

    public void save(Post post) {
        postRepository.save(post);
    }
}
