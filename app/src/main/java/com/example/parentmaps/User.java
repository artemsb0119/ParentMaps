package com.example.parentmaps;

public class User {

    private String id;
    private String name;
    private String lastName;
    private String childOrParent;

    public User(String id, String name, String lastName, String childOrParent) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.childOrParent = childOrParent;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getChildOrParent() {
        return childOrParent;
    }
}
