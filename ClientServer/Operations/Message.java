import UserGroups.ClientUser;
import UserGroups.User;

import java.io.Serializable;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by serge on 20-Mar-17.
 * Used to send all diffrent types of message in one object to avoid 10 else if with instance ofs
 */
public class Message implements Serializable{

    public enum MessageType{
        TEXT, IMAGE, USER_ADD, USER_LIST, LOGIN, USER_REMOVE, ENCRYPTED_TEXT, CHANGE_USERNAME
    }

    private MessageType messageType;

    //default port
    private static final int PORT = 9001;

    private List<ClientUser> userList;
    private int port;
    private byte[] encryptedMessage;
    private String message;
    private LocalDateTime timeSend;
    private byte[] file;
    //private PublicKey publicKey;
    //private String author;
    private User author;

    private Message(){
        author = new User();
        this.timeSend = LocalDateTime.now();
    }

    public Message(String newUsername, PublicKey key) {
        this();
        message = newUsername;
        messageType = MessageType.CHANGE_USERNAME;
        author.setPublicKey(key);
        author.setName(newUsername);
    }

    public Message(String loginUName, String address, int port) {
        this();
        messageType = MessageType.LOGIN;
        author = new User(loginUName);
        message = address;
        this.port = port;
    }

    public Message(User user) {
        this();
        messageType = MessageType.USER_ADD;
        author = user;
    }

    public Message(List<User> userList) {
        this();
        this.userList = new ArrayList<>();
        userList.forEach(user -> this.userList.add(new ClientUser(user)));
        messageType = MessageType.USER_LIST;
    }

    public Message(String message, String author) {
        this();
        messageType = MessageType.TEXT;
        this.message = message;
        this.author = new User(author);
    }

    public Message(byte[] encryptedMessage, String author, PublicKey publicKey) {
        this();
        messageType = MessageType.ENCRYPTED_TEXT;
        this.encryptedMessage = encryptedMessage;
        this.author = new User(author, publicKey);
    }

    public Message(byte[] encryptedMessage, String author, PublicKey publicKey, byte[] file) {
        this(encryptedMessage, author, publicKey);
        this.file = file;
        messageType = MessageType.IMAGE;
    }

    public Message(String message, String author, PublicKey publicKey) {
        this(message, author);
        this.author.setPublicKey(publicKey);
    }

    public Message(String message, String author, PublicKey publicKey, byte[] file) {
        this(message, author, publicKey);
        this.file = file;
        messageType = MessageType.IMAGE;
    }

    static Message createLogoutMessage(User user) {
        Message res = new Message(user);
        res.setMessageType(MessageType.USER_REMOVE);
        return res;
    }

    @Override
    public String toString() {
        return author + ": " + message + "\n" + timeSend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        if(messageType == MessageType.USER_ADD || messageType == MessageType.LOGIN && author != null) {
            return author.getName();
        }
        return null;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(LocalDateTime timeSend) {
        this.timeSend = timeSend;
    }

    public boolean hasImage(){
        return file != null && file.length > 0;
    }

    public static int getPORT() {
        return PORT;
    }

    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public List<ClientUser> getUserList() {
        return userList;
    }

    public void setUserList(List<ClientUser> userList) {
        this.userList = userList;
    }

    public int getPort() {
        return port;
    }

    public PublicKey getPublicKey() {
        return author.getPublicKey();
    }

    public void setPublicKey(PublicKey publicKey) {
        this.author.setPublicKey(publicKey);
    }

    public void setEncryptedMessage(byte[] message) {
        this.encryptedMessage = message;
    }

    public byte[] getEncryptedMessage() {
        return encryptedMessage;
    }
}