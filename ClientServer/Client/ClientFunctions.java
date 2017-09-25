import UserGroups.ClientUser;
import UserGroups.User;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by serge on 20-Mar-17.
 * All the backend functions that one can use in the different front ends
 */
public class ClientFunctions {

    private ClientUser clientUser;

    private PublicKey publicServerKey;

    private Socket socket;

    private String oldAddress;

    private int PORT = Message.getPORT();

    private int oldPort;

    private boolean active;

    private List<User> users = new ArrayList<>();

    public List<Message> getMessages() {
        return messages;
    }

    public List<Message> getNewMessages(int oldAmount) {
        return messages.subList(oldAmount, messages.size());
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    private List<Message> messages = new LinkedList<>();
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean connected = false;

    //Consumers which are used when this active == true
    private Consumer<Message> activeMessageReceiver;
    private Consumer<String> activeUserRemover;
    private Consumer<List<ClientUser>> activeUserAdder;

    //Consumers which are used when this active == false
    private Consumer<Message> passiveMessageReceiver;
    private Consumer<String> passiveUserRemover;
    private Consumer<List<ClientUser>> passiveUserAdder;

    public ClientFunctions(Consumer<Message> passiveMessageReceiver, Consumer<String> passiveUserRemover, Consumer<List<ClientUser>> passiveUserAdder) {
        this.passiveMessageReceiver = passiveMessageReceiver;
        this.passiveUserRemover = passiveUserRemover;
        this.passiveUserAdder = passiveUserAdder;
    }

    public void setActiveConsumers(Consumer<Message> messageConsumer){
        activeMessageReceiver = messageConsumer;
    }

    public ClientFunctions(Consumer<Message> consumer) {
        active = false;
    }

    public void sendNextMessage(ClientUser user) throws IOException {
        outputStream.writeObject(new Message(user.getUuid(), user.getPublicKey()));
    }

    public void sendMessage(String messageToSend) throws IOException {
        outputStream.writeObject(new Message(Security.encrypt(messageToSend, publicServerKey) , clientUser.getName(), clientUser.getPublicKey()));
    }


    /**
     *
     * @param address address where the server lies
     * @return null if a connection could be established,
     * empty string if he tried to connect to an already connected server
     * otherwise return a String with the reason
     */
    public String openConnection (String address, ClientUser user) {
        return openConnection(address, user, PORT);
    }

    public String openConnection (String address, ClientUser user, int port) {
        if(!address.equals(oldAddress) || oldPort != port) { //one should not be able to connect to a server twice
            closeConnection();
            oldAddress = address;
            oldPort = port;
            connected = false;
            users = new ArrayList<>();
            messages = new LinkedList<>();
            try {
                socket = new Socket(address, port); //opens the connection
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                this.clientUser = user;
                if (outputStream != null && inputStream != null) {
                    authenticate();
                    outputStream.writeObject(new Message(clientUser));
                    connected = true;
                    return null;
                }
            } catch (IOException | IllegalArgumentException e) {
                return e.getClass().getSimpleName(); //gets the exception and turns it into an error message
            }
            return "unknown Error"; //if there is an unkown error
        }
        return "";
    }

    public String reOpenConnection(ClientUser user){
        //openConnection(oldAddress, user, oldPort);
        //utputStream.writeObject(new Message(user.getPublicKey()));
        //TODO: implement
        return "";
    }


    public void closeConnection() {
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        messages = new LinkedList<>();
        users = new LinkedList<>();
        connected = false;
    }

    /**
     * @return 0 if it got disconnected, 1 if the server disconnected
     */
    public int waitForMessages () {
        while (connected) {
            try {
                Object object = inputStream.readObject();
                if(object != null && object instanceof Message) {
                    Message message = ((Message) object);
                    switch (message.getMessageType()) {
                        case ENCRYPTED_TEXT:
                            String decryptedMessage = Security.decrypt(message.getEncryptedMessage(), clientUser.getPrivateKey());
                            message.setMessage(decryptedMessage);
                        case TEXT:
                            message.setAuthor(getUserFromList(message.getAuthor()));
                            messages.add(message);
                            chooseActiveOrPassiveConsumer(passiveMessageReceiver, activeMessageReceiver, message);
                            break;
                        case USER_ADD:
                            ClientUser user = new ClientUser(new ClientUser(message.getAuthor()));
                            users.add(user);
                            chooseActiveOrPassiveConsumer(passiveUserAdder, activeUserAdder, Collections.singletonList(user));
                            break;
                        case USER_LIST:
                            List<User> usernameList = message.getUserList();
                            getUsers().addAll(usernameList);
                            chooseActiveOrPassiveConsumer(passiveUserAdder, activeUserAdder, usernameList);
                            break;
                        case USER_REMOVE:
                            this.users.remove(getUserFromList(message.getAuthor()));
                            chooseActiveOrPassiveConsumer(passiveUserRemover, activeUserRemover, message.getAuthor().getName());
                            break;
                        default:
                            break;
                    }
                }
            } catch (EOFException | SocketException e){
                if(connected) {
                    connected = false;
                    return 1;
                }
                return 0;
            }catch (IOException e) {
                connected = false;
                return 0;
            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public boolean isConnected(){
        return connected;
    }

    private void authenticate() {
        if(clientUser != null) {
            Authentication auth;
            Object input;
            boolean inProgress = true;

            while (inProgress) {
                try {
                    input = inputStream.readObject();
                    if (input instanceof Authentication) {
                         auth = ((Authentication) input);
                         publicServerKey = auth.getPublicKey();
                         auth.setPublicKey(clientUser.getPublicKey());
                         auth.setEncryptedMessage(Security.encrypt(auth.getOriginalMessage(), clientUser.getPrivateKey()));
                         outputStream.writeObject(auth);
                         inProgress = false;
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getPORT() {
        return PORT;
    }

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public int getOldPort() {
        return oldPort;
    }

    public void setOldPort(int oldPort) {
        this.oldPort = oldPort;
    }

    public String getAddress() {
        return oldAddress;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * chooses active or passive consumer based on boolean active
     * @param passiveConsumer
     * @param activeConsumer which
     * @param consumable what should be consumed
     */
    private void chooseActiveOrPassiveConsumer(Consumer passiveConsumer, Consumer activeConsumer, Object consumable) {
        if(active){
            activeConsumer.accept(consumable);
        }else {
            if(passiveConsumer != null) {
                passiveConsumer.accept(consumable);
            }
        }
    }

    public void addActiveConsumers (Consumer<Message> activeMessageReceiver, Consumer<String> activeUserRemover, Consumer<List<ClientUser>> activeUserAdder) {
        addActiveConsumers(activeMessageReceiver, activeUserRemover, activeUserAdder, user -> {});
    }

    public void addActiveConsumers (Consumer<Message> activeMessageReceiver, Consumer<String> activeUserRemover, Consumer<List<ClientUser>> activeUserAdder, Consumer<ClientUser> activeChangeUsername) {
        this.activeMessageReceiver = activeMessageReceiver;
        this.activeUserRemover = activeUserRemover;
        this.activeUserAdder = activeUserAdder;
        this.active = true;
    }

    public void removeActiveConsumers (){
        this.activeMessageReceiver = null;
        this.activeUserRemover = null;
        this.activeUserAdder = null;
        this.active = false;
    }

    public void setPassiveMessageReceiver(Consumer<Message> passiveMessageReceiver) {
        this.passiveMessageReceiver = passiveMessageReceiver;
    }

    public ClientUser getClientUser() {
        return clientUser;
    }

    public void setClientUser(ClientUser clientUser) {
        this.clientUser = clientUser;
    }

    private User getUserFromList(User userKey) {
        final User[] result = {userKey};
        users.stream().filter(clientUser -> clientUser.equals(userKey)).forEach(clientUser1 -> result[0] = clientUser1);
        users.forEach(clientUser -> {});
        return result[0];
    }
}