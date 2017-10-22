package com.groendom_chat.groep_technologies.ClientServer.Server;

import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;

import java.util.LinkedList;
import java.util.List;

public class ChatRoom {
    private Handler[] handlers = new Handler[2];
    private List<MessageToSend> messageList = new LinkedList<>();
    private boolean searching = true;

    public ChatRoom(Handler user) {
        handlers[0] = user;
    }

    public boolean addHandler(Handler user) {
        if (searching) {
            handlers[1] = user;
            searching = false;
            return true;
        }
        return false;
    }

    public void addMessage(MessageToSend message) {
        messageList.add(message);
    }

    public Handler[] getHandlers() {
        return handlers;
    }

    public boolean isSearching() {
        return searching;
    }

    public List<MessageToSend> getMessages() {
        return messageList;
    }
}
