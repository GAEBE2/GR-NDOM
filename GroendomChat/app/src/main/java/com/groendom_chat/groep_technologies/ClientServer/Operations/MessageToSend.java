package com.groendom_chat.groep_technologies.ClientServer.Operations;

import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.User;
import com.groendom_chat.groep_technologies.ClientServer.Server.Handler;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by serge on 20-Mar-17.
 * Used to send all diffrent types of message in one object to avoid 10 else if with instance ofs
 */
public class MessageToSend implements Serializable{

    public enum MessageType{
        TEXT, IMAGE, USER_ADD, LOGIN, USER_REMOVE, ENCRYPTED_TEXT, NEXT, RECONNECT
    }

    private MessageType messageType;

    //default port
    private static final int PORT = 9001;

    private List<User> userList;
    private int port;
    private byte[] encryptedMessage;
    private String message;
    private Date timeSend;
    private byte[] file;
    //private PublicKey publicKey;
    //private String author;
    private User author;

    private MessageToSend(){
        author = new User();
        this.timeSend = new Date();
    }



    public MessageToSend(UUID userID, PublicKey publicKey){
        this();
        messageType = MessageType.NEXT;
        author = new User(userID, publicKey);
    }

    public MessageToSend(String loginUName, String address, int port) {
        this();
        messageType = MessageType.LOGIN;
        author = new User(loginUName);
        message = address;
        this.port = port;
    }

    public MessageToSend(User user) {
        this();
        messageType = MessageType.USER_ADD;
        author = user;
    }

    public MessageToSend(String message, String author) {
        this();
        messageType = MessageType.TEXT;
        this.message = message;
        this.author = new User(author);
    }

    public MessageToSend(byte[] encryptedMessage, String author, PublicKey publicKey) {
        this();
        messageType = MessageType.ENCRYPTED_TEXT;
        this.encryptedMessage = encryptedMessage;
        this.author = new User(author, publicKey);
    }

    public MessageToSend(byte[] encryptedMessage, String author, PublicKey publicKey, byte[] file) {
        this(encryptedMessage, author, publicKey);
        this.file = file;
        messageType = MessageType.IMAGE;
    }

    public MessageToSend(String message, String author, PublicKey publicKey) {
        this(message, author);
        this.author.setPublicKey(publicKey);
    }

    public MessageToSend(String message, String author, PublicKey publicKey, byte[] file) {
        this(message, author, publicKey);
        this.file = file;
        messageType = MessageType.IMAGE;
    }

    public static MessageToSend createLogoutMessage(User user) {
        MessageToSend res = new MessageToSend(user);
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

    public Date getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(Date timeSend) {
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

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
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

    public static MessageToSend getReconnectMessage(User user, int arrNumber){
        MessageToSend messageToSend = new MessageToSend(user);
        messageToSend.messageType = MessageType.RECONNECT;
        messageToSend.port = arrNumber;

        return messageToSend;
    }
}