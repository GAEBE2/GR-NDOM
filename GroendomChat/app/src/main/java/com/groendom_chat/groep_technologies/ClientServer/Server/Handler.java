import UserGroups.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Handler extends Thread {
    private User user;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private int roomIndex;
    private KeyPair keyPair;
    private List<ChatRoom> roomList;
    private List<User> userList;
    private Consumer<Handler> disconnect;

    public Handler(Socket socket, KeyPair keyPair, List<ChatRoom> chatRooms, List<User> userList, Consumer<Handler> disconnect) {
        this.socket = socket;
        this.roomList = chatRooms;
        this.userList = userList;
        this.keyPair = keyPair;
        this.disconnect = disconnect;
    }

    /**
     * the handler for a socket
     */
    public void run() {
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey publicKey = getPublicKeyFromClient();
            user = getUserFromClient();

            userList.add(user);
            roomIndex = insetIntoRoom();

            //send active users on server to user
            outputStream.writeObject(new Message(roomList.get(roomIndex).getHandlers()));

            //send new user to active users
            for (Handler handler : roomList.get(roomIndex).getHandlers()) {
                if(handler != null) {
                    handler.outputStream.writeObject(new Message(user));
                }
            }

            //send all stored messages
            String originalMessage;
            for (Message message : roomList.get(roomIndex).getMessages()) {
                originalMessage = message.getMessage();
                if(originalMessage != null) {
                    message.setEncryptedMessage(Security.encrypt(originalMessage, publicKey));
                    message.setMessage(null);
                    message.setMessageType(Message.MessageType.ENCRYPTED_TEXT);
                    message.setPublicKey(keyPair.getPublic());
                    outputStream.writeObject(message);
                }
                message.setMessage(originalMessage);
                message.setPublicKey(null);
                message.setEncryptedMessage(null);
            }

            ServerFunctions.log("client connected; IP: " + socket.getRemoteSocketAddress().toString() + " | username: " + user.getName());
            receiveMessagesAndForwardThem();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            handleDisconnect();
        }
    }

    public int insetIntoRoom(){
        if(roomList == null){
            roomList = new ArrayList<>();
        }
        if(roomList.size() != 0 && roomList.get(roomList.size() - 1).isSearching()){
            roomList.get(roomList.size() - 1).addHandler(this);
        }else {
            roomList.add(new ChatRoom(this));
        }
        return roomList.size() - 1;
    }

    /**
     * get public key from client and checks whether or not it's valid key and the client processes the fitting private key
     * @return public key
     * @throws IOException
     */
    public PublicKey getPublicKeyFromClient() throws IOException {
        PublicKey result = null;
        Authentication authentication = new Authentication(Security.randomlyGenerateAString());
        authentication.setPublicKey(keyPair.getPublic());
        outputStream.writeObject(authentication);
        while (result == null){
            try {
                Object object = inputStream.readObject();
                if(object != null && object instanceof Authentication
                        && ((Authentication) object).getOriginalMessage().equals(authentication.getOriginalMessage())
                        && Objects.equals(Security.decrypt(((Authentication) object).getEncryptedMessage(),((Authentication) object).getPublicKey()),
                        ((Authentication) object).getOriginalMessage())) {
                    result = ((Authentication) object).getPublicKey();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     *
     * @return username from user
     * @throws IOException
     */
    public User getUserFromClient() throws IOException {
        User result = null;
        while (result == null) {
            try {
                Object object = inputStream.readObject();
                if(object != null) {
                    Message message = ((Message) object);
                    result = message.getAuthor();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * receives messages and forwards them to the other clients, with the right encryption
     * @throws IOException
     */
    public void receiveMessagesAndForwardThem() throws IOException {
        while (true) {
            try {
                Object object = inputStream.readObject();
                if(object == null){
                    return;
                }
                Message message = ((Message) object);
                roomList.get(roomIndex).addMessage(message);
                sendMessageToEveryone(message);
                ServerFunctions.log("message: " + message.getMessage() + " author: " + message.getAuthor() + "|| Send to: " + userList.size() + " clients");
            } catch (ClassNotFoundException e) {
            }
        }
    }

    public void handleDisconnect() {
        try {
            if(userList != null) {
                userList.remove(user);
                disconnect.accept(this);
                //tell everyone that user left the channel
                sendMessageToEveryone(Message.createLogoutMessage(user));
            }
            ServerFunctions.log("client disconnected; IP: ");
            if(socket.getRemoteSocketAddress() != null && user != null) {
                ServerFunctions.log(socket.getRemoteSocketAddress().toString() + " | username: " + user.getName());
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send a message to all active users
     * @param message message to send
     * @throws IOException
     */
    public void sendMessageToEveryone(Message message) throws IOException{
        String decryptedMessage = "";
        if(message.getEncryptedMessage() != null) {
            decryptedMessage = Security.decrypt(message.getEncryptedMessage(), keyPair.getPrivate());
        }

        if(roomList.size() != 0) {
            for (Handler handler : roomList.get(roomIndex).getHandlers()) {
                try {
                    if (handler != null){
                        if (message.getMessageType() == Message.MessageType.ENCRYPTED_TEXT) {
                            message.setEncryptedMessage(Security.encrypt(decryptedMessage, handler.user.getPublicKey()));
                        }
                        handler.outputStream.writeObject(message);
                    }
                } catch (SocketException ignored) { //ignores disconnection

                }
            }
            message.setMessage("".equals(decryptedMessage) ? message.getMessage() : decryptedMessage);
        }
    }

    public User getUser(){
        return user;
    }

}