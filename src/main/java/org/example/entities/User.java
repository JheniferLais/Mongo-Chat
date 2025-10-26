package org.example.entities;

public class User {
    private final String nickname;
    private final String email;
    private final String password;

    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public String getNickname() { return this.nickname; }
    public String getEmail() { return this.email; }
    public String getPassword() { return this.password; }

    @Override
    public String toString() {
        return "User{" +
                "nickname='" + this.nickname + '\'' +
                ", email='" + this.email + '\'' +
                '}';
    }
}
