package ru.otus.multipledbdemo.domain.primary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class User {

    @Column(name = "user_id")
    private long id;

    @Transient
    private String name;

    @Transient
    private String login;

    public void setNameAndLogin(String name, String login) {
        this.name = name;
        this.login = login;
    }
}
