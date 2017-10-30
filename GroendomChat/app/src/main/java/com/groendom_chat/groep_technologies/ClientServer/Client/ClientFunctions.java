package com.groendom_chat.groep_technologies.ClientServer.Client;

import android.os.AsyncTask;

import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.User;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Authentication;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by serge on 20-Mar-17.
 * All the backend functions that one can use in the different front ends
 * Needs serializable to get transferred between activities
 */
public class ClientFunctions {

    private ClientUser clientUser;

    private PublicKey publicServerKey;

    private String oldAddress;

    private int PORT = MessageToSend.getPORT();

    private int oldPort;

    private boolean active = false;

    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private List<User> users = new ArrayList<>();
    private List<MessageToSend> messages = new LinkedList<>();
    private boolean connected = false;
    //Consumers which are used when this active == true
    private Consumer<MessageToSend> activeMessageReceiver;
    private Consumer<String> activeUserRemover;
    private Consumer<List<ClientUser>> activeUserAdder;
    //Consumers which are used when this active == false
    private Consumer<MessageToSend> passiveMessageReceiver;
    private Consumer<String> passiveUserRemover;
    private Consumer<List<ClientUser>> passiveUserAdder;

    public ClientFunctions(Consumer<MessageToSend> passiveMessageReceiver, Consumer<String> passiveUserRemover, Consumer<List<ClientUser>> passiveUserAdder) {
        this.passiveMessageReceiver = passiveMessageReceiver;
        this.passiveUserRemover = passiveUserRemover;
        this.passiveUserAdder = passiveUserAdder;
    }

    public ClientFunctions(Consumer<MessageToSend> consumer) {
        passiveMessageReceiver = consumer;
        active = false;
    }

    public List<MessageToSend> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageToSend> messages) {
        this.messages = messages;
    }

    public List<MessageToSend> getNewMessages(int oldAmount) {
        return messages.subList(oldAmount, messages.size());
    }

    public void setActiveConsumers(Consumer<MessageToSend> messageConsumer) {
        activeMessageReceiver = messageConsumer;
        active = true;
    }

    public void sendNextMessage(ClientUser client) throws IOException {
        outputStream.writeObject(new MessageToSend(client.getUser().getUuid(), client.getUser().getPublicKey()));
    }

    /**
     * sends message in another thread
     * @param messageToSend string that want's be send
     * @return if successfully
     * @throws IOException
     */
    public boolean sendMessage(String messageToSend) throws IOException {
        SendTask task = new SendTask();
        try {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, messageToSend).get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return false;
            //return task.execute().get();
        }
    }

    /**
     * sends a request to be put in a new room, to the server, in an other task
     * @return if successfully
     */
    public boolean requestNewRoom() {
        NewRoomTask task = new NewRoomTask();
        try {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
            return false;
            //return task.execute().get();
        }
    }

    /**
     * @param address address where the server lies
     * @return null if a connection could be established,
     * empty string if he tried to connect to an already connected server
     * otherwise return a String with the reason
     * curActivity is used to create a toast on connection error/success - null if not used in activity
     */
    public void openConnection(String address, ClientUser client, Callback cb) {
        this.clientUser = client;
        OpenConnectionTask task = new OpenConnectionTask();
        try {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, address, cb).get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * sed to be
     * @param client client that wants to reconnect
     * @return message
     */
    public String reOpenConnection(ClientUser client) {
        //openConnection(oldAddress, user, oldPort);
        //utputStream.writeObject(new MessageToSend(user.getPublicKey()));
        //TODO: implement
        return "";
    }

    /**
     * closes the connection, surprising i know
     */
    public void closeConnection() {
        if (socket != null) {
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
     * needs to be executed in a different thread !
     * @return 0 if it got disconnected, 1 if the server disconnected
     */
    public int waitForMessages() {
        while (connected) {
            try {
                Object object = inputStream.readObject();
                if (object != null && object instanceof MessageToSend) {
                    MessageToSend message = ((MessageToSend) object);
                    switch (message.getMessageType()) {
                        case ENCRYPTED_TEXT:
                            String decryptedMessage = Security.decrypt(message.getEncryptedMessage(), clientUser.getPrivateKey());
                            message.setMessage(decryptedMessage);
                        case TEXT:
                            //message.setAuthor(getUserFromList(message.getAuthor()));
                            messages.add(message);
                            chooseActiveOrPassiveConsumer(passiveMessageReceiver, activeMessageReceiver, message);
                            break;
                        case USER_ADD:
                            ClientUser user = new ClientUser(message.getAuthor());
                            users.add(user.getUser());
                            chooseActiveOrPassiveConsumer(passiveUserAdder, activeUserAdder, Collections.singletonList(user));
                            break;
                        case USER_REMOVE:
                            this.users.remove(getUserFromList(message.getAuthor()));
                            chooseActiveOrPassiveConsumer(passiveUserRemover, activeUserRemover, message.getAuthor().getName());
                            break;
                        default:
                            break;
                    }
                }
            } catch (EOFException | SocketException e) {
                if (connected) {
                    connected = false;
                    return 1;
                }
                return 0;
            } catch (IOException e) {
                connected = false;
                return 0;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return 1;
            }
        }
        return 0;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * used to authenticate on server, using public/private key
     */
    private void authenticate() {
        if (clientUser != null) {
            Authentication auth;
            Object input;
            boolean inProgress = true;

            while (inProgress) {
                try {
                    input = inputStream.readObject();
                    if (input instanceof Authentication) {
                        auth = ((Authentication) input);
                        publicServerKey = Security.byteArrToPublicKey(auth.getPublicKey());
                        auth.setPublicKey(clientUser.getUser().getPublicKey());
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

    public void setObjectInputStream(ObjectInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * chooses active or passive consumer based on boolean active
     *
     * @param passiveConsumer
     * @param activeConsumer  which
     * @param consumable      what should be consumed
     */
    private void chooseActiveOrPassiveConsumer(Consumer passiveConsumer, Consumer activeConsumer, Object consumable) {
        if (active) {
            activeConsumer.accept(consumable);
        } else {
            if (passiveConsumer != null) {
                passiveConsumer.accept(consumable);
            }
        }
    }

    public void addActiveConsumers(Consumer<MessageToSend> activeMessageReceiver, Consumer<String> activeUserRemover, Consumer<List<ClientUser>> activeUserAdder) {
        addActiveConsumers(activeMessageReceiver, activeUserRemover, activeUserAdder, null);
        //null was before: user -> {});
    }

    public void addActiveConsumers(Consumer<MessageToSend> activeMessageReceiver, Consumer<String> activeUserRemover, Consumer<List<ClientUser>> activeUserAdder, Consumer<ClientUser> activeChangeUsername) {
        this.activeMessageReceiver = activeMessageReceiver;
        this.activeUserRemover = activeUserRemover;
        this.activeUserAdder = activeUserAdder;
        this.active = true;
    }

    public void removeActiveConsumers() {
        this.activeMessageReceiver = null;
        this.activeUserRemover = null;
        this.activeUserAdder = null;
        this.active = false;
    }

    public void setPassiveMessageReceiver(Consumer<MessageToSend> passiveMessageReceiver) {
        this.passiveMessageReceiver = passiveMessageReceiver;
    }

    public ClientUser getClientUser() {
        return clientUser;
    }

    public void setClientUser(ClientUser clientUser) {
        this.clientUser = clientUser;
    }

    /**
     *
     * @param userKey User to be searched
     * @return User found
     */
    private User getUserFromList(User userKey) {
        final User[] result = {userKey};
        for (User clientUser : users) {
            if (clientUser.equals(userKey)) {
                result[0] = clientUser;
            }
        }
        return result[0];
    }

    /**
     * Task to open a new connection
     */
    private class OpenConnectionTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            String address = params[0];
            if (!address.equals(oldAddress) || oldPort != PORT) { //one should not be able to connect to a server twice
                closeConnection();
                oldAddress = address;
                oldPort = PORT;
                connected = false;
                users = new ArrayList<>();
                messages = new LinkedList<>();
                try {
                    socket = new Socket(address, PORT); //opens the connection
                    outputStream = new ObjectOutputStream(socket.getOutputStream());
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    if (outputStream != null) {
                        //authenticate();
                        outputStream.writeObject(new MessageToSend(clientUser.getUser()));
                        connected = true;
                    }
                } catch (IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                    return e.getClass().getSimpleName(); //gets the exception and turns it into an error message
                }
            }
        }
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    private class OpenConnectionTask extends AsyncTask<Object, Void, Void> {
        protected Void doInBackground(Object... params) {
            String address = (String) params[0];
            Callback cb = (Callback) params[1];
            closeConnection();
            oldAddress = address;
            oldPort = PORT;
            connected = false;
            users = new ArrayList<>();
            messages = new LinkedList<>();
            try {
                socket = new Socket(address, PORT); //opens the connection
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                //authenticate();
                outputStream.writeObject(new MessageToSend(clientUser.getUser()));
                if (cb != null) {
                    cb.onFinish(socket, outputStream, inputStream);
                }
                connected = true;
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                if (cb != null) {
                    cb.onFinish(e);
                }
            }
            return null;
        }
    }

    /**
     * Task to send a message
     */
    private class SendTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... params) {
            for (String messageToSend : params) {
                try {
                    //outputStream.writeObject(new MessageToSend(Security.encrypt(messageToSend, publicServerKey), clientUser.getName(), clientUser.getPublicKey()));
                    outputStream.writeObject(new MessageToSend(messageToSend, clientUser.getUser()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Task to send a newRoomRequest
     */
    private class NewRoomTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... params) {
            try {
                outputStream.writeObject(new MessageToSend(MessageToSend.MessageType.NEW_ROOM));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}