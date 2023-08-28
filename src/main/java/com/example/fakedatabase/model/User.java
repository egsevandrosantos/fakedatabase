package com.example.fakedatabase.model;

public class User {
    private String id;
    private String email;

    public User(String id, String email) {
        setId(id);
        setEmail(email);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
