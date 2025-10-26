package org.example.entities;

public class Message {
    private String fromEmail;
    private String toEmail;
    private String message;

    public Message(String nicknameFrom, String nicknameTo, String message) {
        this.fromEmail = nicknameFrom;
        this.toEmail = nicknameTo;
        this.message = message;
    }

    public String getFromEmail() { return this.fromEmail; }
    public String getToEmail() { return this.toEmail; }
    public String getMessage() { return this.message; }

    @Override
    public String toString() {
        return this.fromEmail + " -> " + this.toEmail + ": " + this.message;
    }
}
