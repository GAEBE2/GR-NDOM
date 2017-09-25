import UserGroups.User;

import java.util.LinkedList;
import java.util.List;

public class ChatRoom {
    private Handler[] handlers = new Handler[2];
    private List<Message> messageList = new LinkedList<>();
    private boolean searching = true;

    public ChatRoom(Handler user) {
        handlers[0] = user;
    }

    public boolean addHandler(Handler user){
        if(searching){
            handlers[1] = user;
            searching = false;
            return true;
        }
        return false;
    }

    public void addMessage(Message message){
        messageList.add(message);
    }

    public Handler[] getHandlers(){
        return handlers;
    }

    public boolean isSearching() {
        return searching;
    }

    public List<Message> getMessages(){
        return messageList;
    }
}
