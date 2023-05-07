package com.example.parentmaps;

public class Friends {
    private String userId1;
    private String userId2;

    public Friends() {
    }

    public Friends(String userId1, String userId2) {
        this.userId1 = userId1;
        this.userId2 = userId2;
    }

    public String getUserId1() {
        return userId1;
    }

    public String getUserId2() {
        return userId2;
    }
}
