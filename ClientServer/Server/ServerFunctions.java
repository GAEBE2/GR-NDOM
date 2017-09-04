import UserGroups.User;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by serge on 20-Mar-17.
 */
public class ServerFunctions {
    static List<Message> messageList = new LinkedList<>();
    static List<Handler> handlerList = new ArrayList<>();
    static List<User> userList = new ArrayList<>();
    static PublicKey publicServerKey;
    private static Logger LOG = Logger.getLogger("");
    static PrivateKey privateServerKey;

    private static boolean open = true;

    private static int port = Message.getPORT(); //standard

    public static void main(String[] args) throws IOException {
        LOG.setLevel(Level.ALL);
        //set custom port and public/private key dir
        for (String string : args) {
            if(NumberUtils.isParsable(string)) {
                port = Integer.parseInt(string);
            }
        }
        generateNewKeysIfNecessary();
        
        log("The chat server is running on port: " + port);
        try (ServerSocket listener = new ServerSocket(port)) {
            while (open) {
                Handler handler = new Handler(listener.accept());
                handlerList.add(handler);
                handler.start();
            }
        }
    }

    private static class Handler extends Thread {
        private User user;
        private Socket socket;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * the handler for a socket
         */
        public void run() {
            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                PublicKey publicKey = getPublicKeyFromClient();
                String username = getUsernameFromClient();
                user = new User(username, publicKey);

                //send active users on server to user
                outputStream.writeObject(new Message(userList));

                //send new user to active users
                for (Handler handler : handlerList) {
                    handler.outputStream.writeObject(new Message(user));
                }
                userList.add(user);

                //send all stored messages
                String originalMessage;
                for (Message message : messageList) {
                    originalMessage = message.getMessage();
                    if(originalMessage != null) {
                        message.setEncryptedMessage(Security.encrypt(originalMessage, publicKey));
                        message.setMessage(null);
                        message.setMessageType(Message.MessageType.ENCRYPTED_TEXT);
                        message.setPublicKey(publicServerKey);
                        outputStream.writeObject(message);
                    }
                    message.setMessage(originalMessage);
                    message.setPublicKey(null);
                    message.setEncryptedMessage(null);
                }

                log("client connected; IP: " + socket.getRemoteSocketAddress().toString() + " | username: " + username);
                receiveMessagesAndForwardThem();
            } catch (IOException e) {
            } finally {
                handleDisconnect();
            }
        }

        /**
         * get public key from client and checks whether or not it's valid key and the client processes the fitting private key
         * @return public key
         * @throws IOException
         */
        public PublicKey getPublicKeyFromClient() throws IOException {
            PublicKey result = null;
            Authentication authentication = new Authentication(Security.randomlyGenerateAString());
            authentication.setPublicKey(publicServerKey);
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
        public String getUsernameFromClient() throws IOException {
            String result = "";
            while (result.equals("")) {
                try {
                    Object object = inputStream.readObject();
                    if(object != null) {
                        Message message = ((Message) object);
                        if(message.getMessageType() == Message.MessageType.LOGIN) {
                            result = message.getUsername();
                        }
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
                    messageList.add(message);
                    sendMessageToEveryone(message);
                    log("message: " + message.getMessage() + " author: " + message.getAuthor() + "|| Send to: " + userList.size() + " clients");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        public void handleDisconnect() {
            try {
                if(userList != null) {
                    userList.remove(user);
                    handlerList.remove(this);
                    //tell everyone that user left the channel
                    sendMessageToEveryone(Message.createLogoutMessage(user));
                }
                log("client disconnected; IP: " + socket.getRemoteSocketAddress().toString() + " | username: " + user.getName());
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
                decryptedMessage = Security.decrypt(message.getEncryptedMessage(), privateServerKey);
            }

            for (Handler handler : handlerList) {
                try {
                    if(message.getMessageType() == Message.MessageType.ENCRYPTED_TEXT) {
                        message.setEncryptedMessage(Security.encrypt(decryptedMessage, handler.user.getPublicKey()));
                    }
                    handler.outputStream.writeObject(message);
                }catch (SocketException ignored){ //ignores disconnection

                }
            }
            message.setMessage("".equals(decryptedMessage) ? message.getMessage() : decryptedMessage);
        }
    }

    public static void log(String msg) {
        LOG.log(Level.ALL, msg);
        System.out.println(msg);
    }

    /**
     * generates a new keypair for the server
     */
    private static void generateNewKeysIfNecessary() {
        if(privateServerKey == null) {
            try {
                KeyPair pair = Security.generateKeyPair();
                privateServerKey = pair.getPrivate();
                publicServerKey = pair.getPublic();
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                log("error generating new keyPair");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}