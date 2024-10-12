package ru.gulash.spring.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    private String content;

    @DBRef(lazy = false/*default*/)
    private User user;  // связь с User через @DBRef

    public Post() {}

    public Post(String content, User user) {
        this.content = content;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Post{" +
            "id='" + id + '\'' +
            ", content='" + content + '\'' +
            ", user=" + user +
            '}';
    }
}
