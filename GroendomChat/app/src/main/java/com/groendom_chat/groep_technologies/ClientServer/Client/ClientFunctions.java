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
import java.io.Serializable;
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

import static android.R.attr.port;

/**
 * Created by serge on 20-Mar-17.
 * All the backend functions that one can use in the different front ends
 * Needs serializable to get transferred between activities
 */
public class ClientFunctions implements Serializable {

    private ClientUser clientUser;

    private PublicKey publicServerKey;

    private Socket socket;

    private String oldAddress;

    private int PORT = MessageToSend.getPORT();

    private int oldPort;

    private boolean active = false;

    private List<User> users = new ArrayList<>();

    public List<MessageToSend> getMessages() {
        return messages;
    }

    public List<MessageToSend> getNewMessages(int oldAmount) {
        return messages.subList(oldAmount, messages.size());
    }

    public void setMessages(List<MessageToSend> messages) {
        this.messages = messages;
    }

    private List<MessageToSend> messages = new LinkedList<>();
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
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

    public void setActiveConsumers(Consumer<MessageToSend> messageConsumer) {
        activeMessageReceiver = messageConsumer;
        active = true;
    }

    public ClientFunctions(Consumer<MessageToSend> consumer) {
        passiveMessageReceiver = consumer;
        active = false;
    }

    public void sendNextMessage(ClientUser client) throws IOException {
        outputStream.writeObject(new MessageToSend(client.getUser().getUuid(), client.getUser().getPublicKey()));
    }

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
     * @param address address where the server lies
     * @return null if a connection could be established,
     * empty string if he tried to connect to an already connected server
     * otherwise return a String with the reason
     * curActivity is used to create a toast on connection error/success - null if not used in activity
     */
    public void openConnection(String address, ClientUser client) {
        this.clientUser = client;
        OpenConnectionTask task = new OpenConnectionTask();
        try {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, address, cb).get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public String reOpenConnection(ClientUser client) {
        //openConnection(oldAddress, user, oldPort);
        //utputStream.writeObject(new MessageToSend(user.getPublicKey()));
        //TODO: implement
        return "";
    }


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

    private User getUserFromList(User userKey) {
        final User[] result = {userKey};
        for (User clientUser : users) {
            if (clientUser.equals(userKey)) {
                result[0] = clientUser;
            }
        }
        return result[0];
    }

    private class OpenConnectionTask extends AsyncTask<Object, Void, Void> implements Serializable {
        protected Void doInBackground(Object... params) {
            String address = (String) params[0];
            Callback cb = (Callback) params[1];
            if (address != null && !address.equals(oldAddress) || oldPort != PORT) { //one should not be able to connect to a server twice
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
                        if (cb != null) {
                            cb.onFinish(null);
                        }
                        connected = true;
                    }
                } catch (IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                    if (cb != null) {
                        cb.onFinish(e);
                    }
                }
            }

            if(cb != null) {
                cb.onFinish(new Exception("Same address or same port used or address is null"));
            }
            return null;
        }
    }

    private class SendTask extends AsyncTask<String, Void, Boolean> implements Serializable {
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
}