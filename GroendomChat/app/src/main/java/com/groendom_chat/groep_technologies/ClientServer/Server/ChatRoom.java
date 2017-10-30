package com.groendom_chat.groep_technologies.ClientServer.Server;

import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;

import java.util.LinkedList;
import java.util.List;

public class ChatRoom {
    private Handler[] handlers = new Handler[2];
    private List<MessageToSend> messageList = new LinkedList<>();
    private boolean searching = true;
    int freePlace;

    public ChatRoom(Handler user) {
        handlers[0] = user;
        freePlace = 1;
    }

    /**
     * adds a handler if possible
     * @param user handler to add
     * @return if it was added
     */
    public boolean addHandler(Handler user) {
        if (searching) {
            handlers[freePlace] = user;
            searching = false;
            return true;
        }
        return false;
    }

    public void removeHandler(Handler handler){
        for (int i = 0; i < handlers.length; i++) {
            if(handlers[i].equals(handler)){
                removeHandler(i);
            }
        }
    }

    public void removeHandler(int index){
        switch (index){
            case 0:
            case 1:
                handlers[index] = null;
                searching = true;
                break;
        }
        freePlace = index;
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
