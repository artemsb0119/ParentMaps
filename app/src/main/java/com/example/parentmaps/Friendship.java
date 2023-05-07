package com.example.parentmaps;

public class Friendship {

    private String senderId;
    private String receiverId;
    private String status;

    public Friendship() {
    }

    public Friendship(String senderId, String receiverId, String status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getStatus() {
        return status;
    }
}
