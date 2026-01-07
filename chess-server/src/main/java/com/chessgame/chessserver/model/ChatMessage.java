package com.chessgame.chessserver.model;

/**
 * Message representing a chat message.
 */
public class ChatMessage {
    private String type = "chat";
    private String sender;
    private String text;

    public ChatMessage() {
    }

    public ChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
