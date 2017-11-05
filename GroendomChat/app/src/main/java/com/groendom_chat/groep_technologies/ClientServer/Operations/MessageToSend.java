package com.groendom_chat.groep_technologies.ClientServer.Operations;

import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.User;

import java.io.Serializable;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by serge on 20-Mar-17.
 * Used to send all diffrent types of message in one object to avoid 10 else if with instance ofs
 */
public class MessageToSend implements Serializable {

  public enum MessageType {
    TEXT, IMAGE, USER_ADD, LOGIN, USER_LEFT, ENCRYPTED_TEXT, RECONNECT, NEW_ROOM
  }

  //default port
  private static final int PORT = 9001;
  private MessageType messageType;
  private int port;
  private byte[] encryptedMessage;
  private String message;
  private Date timeSend;
  private byte[] file;
  private User author;

  private MessageToSend() {
    author = new User();
    this.timeSend = new Date();
  }

  public static MessageToSend createNextMessage(UUID userID, PublicKey publicKey) {
    MessageToSend res = new MessageToSend();
    res.messageType = MessageType.TEXT;
    res.author = new User(userID, publicKey);
    return res;
  }

  public static MessageToSend createEncrpytedImageMessage(byte[] encryptedMessage, User author, byte[] file) {
    MessageToSend res = createEncryptedTextMessage(encryptedMessage, author);
    res.file = file;
    res.messageType = MessageType.IMAGE;
    return res;
  }

  public static MessageToSend createImageMessage(String message, User author, byte[] file) {
    MessageToSend res = createTextMessage(message, author);
    res.file = file;
    res.messageType = MessageType.IMAGE;
    return res;
  }

  public static MessageToSend createLoginMessage(String loginUName, String address, int port) {
    MessageToSend res = new MessageToSend();
    res.messageType = MessageType.LOGIN;
    res.author = new User(loginUName);
    res.message = address;
    res.port = port;
    return res;
  }

  public static MessageToSend createEncryptedTextMessage(byte[] encryptedMessage, User author){
    MessageToSend res = new MessageToSend();
    res.messageType = MessageType.ENCRYPTED_TEXT;
    res.encryptedMessage = encryptedMessage;
    res.author = author;
    return res;
  }

  public static MessageToSend createTextMessage(String message, User author){
    MessageToSend res = new MessageToSend();
    res.messageType = MessageType.TEXT;
    res.message = message;
    res.author = author;
    return res;
  }

  public static MessageToSend createUserAmountChangeMessage(User user, int usersAmount, MessageType type) {
    MessageToSend res = new MessageToSend();
    res.messageType = type;
    res.author = user;
    res.setMessage(String.valueOf(usersAmount));
    return res;
  }

  public static MessageToSend createUserAddMessage(User user, int usersAmount) {
    return createUserAmountChangeMessage(user, usersAmount, MessageType.USER_ADD);
  }

  public static MessageToSend createLogoutMessage(User user, int usersAmount) {
    return createUserAmountChangeMessage(user, usersAmount, MessageType.USER_LEFT);
  }

  public static MessageToSend createSwitchRoomMessage() {
    MessageToSend res = new MessageToSend();
    res.messageType = MessageType.NEW_ROOM;
    return res;
  }

  public static MessageToSend getReconnectMessage(User user, int arrNumber) {
    MessageToSend messageToSend = MessageToSend.createUserAddMessage(user, 0);
    messageToSend.messageType = MessageType.RECONNECT;
    messageToSend.port = arrNumber;

    return messageToSend;
  }


  public static int getPORT() {
    return PORT;
  }


  /**
   * Format date string
   *
   * @return formatted date string
   */
  public String getDateString() {
    if (getTimeSend().before(getToday())) {
      return new SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
          .format(getTimeSend());
    } else {
      return new SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(getTimeSend());
    }
  }

  private Date getToday() {
    Calendar c = Calendar.getInstance();

    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c.getTime();
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
    if (messageType == MessageType.USER_ADD || messageType == MessageType.LOGIN && author != null) {
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

  public boolean hasImage() {
    return file != null && file.length > 0;
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

  public int getPort() {
    return port;
  }

  public PublicKey getPublicKey() {
    return author.getPublicKey();
  }

  public void setPublicKey(PublicKey publicKey) {
    this.author.setPublicKey(publicKey);
  }

  public byte[] getEncryptedMessage() {
    return encryptedMessage;
  }

  public void setEncryptedMessage(byte[] message) {
    this.encryptedMessage = message;
  }
}