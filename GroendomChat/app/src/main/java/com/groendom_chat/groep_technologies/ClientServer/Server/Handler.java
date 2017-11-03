package com.groendom_chat.groep_technologies.ClientServer.Server;

import com.groendom_chat.groep_technologies.ClientServer.Client.Consumer;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.User;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Authentication;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Objects;

public class Handler extends Thread {

  private User user;
  private Socket socket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;
  private int roomIndex;
  private KeyPair keyPair;
  private Consumer<Handler> disconnect;

  public Handler(Socket socket, KeyPair keyPair, Consumer<Handler> disconnect) {
    this.socket = socket;
    this.keyPair = keyPair;
    this.disconnect = disconnect;
  }

  private int getHandlersSize(Handler[] handlers) {
    int size = 0;
    for(Handler handler : handlers) {
      if(handler != null) {
        size++;
      }
    }
    return size;
  }

  /**
   * the handler for a socket
   */
  public void run() {
    try {
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      inputStream = new ObjectInputStream(socket.getInputStream());
      //PublicKey publicKey = getPublicKeyFromClient();
      PublicKey publicKey = null;
      user = getUserFromClient();

      ServerFunctions.userList.add(user);
      roomIndex = ServerFunctions.insetIntoRoom(this);

      Handler[] handlers = ServerFunctions.roomList.get(roomIndex).getHandlers();
      //send new user to active users
      for (Handler handler : handlers) {
        if (handler != null) {
          handler.outputStream.writeObject(MessageToSend.createUserAddMessage(handler.getUser(), getHandlersSize(handlers)));
        }
      }

      //send all stored messages
      String originalMessage;
      for (MessageToSend messageToSend : ServerFunctions.roomList.get(roomIndex).getMessages()) {
        originalMessage = messageToSend.getMessage();
        if (originalMessage != null) {
          messageToSend.setEncryptedMessage(Security.encrypt(originalMessage, publicKey));
          messageToSend.setMessage(null);
          messageToSend.setMessageType(MessageToSend.MessageType.ENCRYPTED_TEXT);
          messageToSend.setPublicKey(keyPair.getPublic());
          outputStream.writeObject(messageToSend);
        }
        messageToSend.setMessage(originalMessage);
        messageToSend.setPublicKey(null);
        messageToSend.setEncryptedMessage(null);
      }

      ServerFunctions.log("client connected; IP: " +
          socket.getRemoteSocketAddress().toString() +
          " | username: " + user.getName());
      receiveMessagesAndForwardThem();
    } catch (IOException e) {
      ServerFunctions.log("");
      e.printStackTrace();
    } finally {
      handleDisconnect();
    }
  }


  /**
   * get public key from client and checks whether or not it's valid key and the client processes
   * the fitting private key
   *
   * @return public key
   */
  public PublicKey getPublicKeyFromClient() throws IOException {
    PublicKey result = null;
    Authentication authentication = new Authentication(Security.randomlyGenerateAString());
    authentication.setPublicKey(keyPair.getPublic());
    outputStream.writeObject(authentication);
    while (result == null) {
      try {
        Object object = inputStream.readObject();
        if (object != null && object instanceof Authentication
            && ((Authentication) object).getOriginalMessage()
            .equals(authentication.getOriginalMessage())
            && Objects.equals(Security.decrypt(((Authentication) object).getEncryptedMessage(),
            Security.byteArrToPublicKey(((Authentication) object).getPublicKey())),
            ((Authentication) object).getOriginalMessage())) {
          result = Security.byteArrToPublicKey(((Authentication) object).getPublicKey());
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  /**
   * @return username from user
   */
  public User getUserFromClient() throws IOException {
    User result = null;
    while (result == null) {
      try {
        Object object = inputStream.readObject();
        if (object != null) {
          MessageToSend messageToSend = ((MessageToSend) object);
          result = messageToSend.getAuthor();
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  /**
   * receives messages and forwards them to the other clients, with the right encryption
   */
  public void receiveMessagesAndForwardThem() throws IOException {
    while (true) {
      try {
        Object object = inputStream.readObject();
        System.out.println(object);
        if (object != null && object instanceof MessageToSend) {
          MessageToSend messageToSend = ((MessageToSend) object);
          if (messageToSend.getMessageType() == MessageToSend.MessageType.NEW_ROOM) {
            ServerFunctions.insertIntoNewRoom(this);
          } else {

            ServerFunctions.roomList.get(roomIndex).addMessage(messageToSend);
            sendMessageToEveryone(messageToSend);
            ServerFunctions.log("message: " + messageToSend.getMessage() + " author: "
                + messageToSend.getAuthor().getName() + " | Send to room index: " + roomIndex);
          }
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * removes it form all lists, sends a diconnect message
   */
  public void handleDisconnect() {
    try {
      if (ServerFunctions.userList != null) {
        ServerFunctions.userList.remove(user);
        disconnect.accept(this);
        //tell everyone that user left the channel
        sendMessageToEveryone(MessageToSend.createLogoutMessage(user, ServerFunctions.roomList.get(roomIndex).getHandlers().length));
      }
      ServerFunctions.log("client disconnected; IP: ");
      if (socket.getRemoteSocketAddress() != null && user != null) {
        ServerFunctions
            .log(socket.getRemoteSocketAddress().toString() + " | username: " + user.getName());
      }
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * send a messageToSend to all active users
   *
   * @param messageToSend messageToSend to send
   */
  public void sendMessageToEveryone(MessageToSend messageToSend) throws IOException {
    String decryptedMessage = "";
    if (messageToSend.getEncryptedMessage() != null) {
      decryptedMessage = Security
          .decrypt(messageToSend.getEncryptedMessage(), keyPair.getPrivate());
    }

    if (ServerFunctions.roomList.size() != 0) {
      for (Handler handler : ServerFunctions.roomList.get(roomIndex).getHandlers()) {
        try {
          if (handler != null) {
            if (messageToSend.getMessageType() == MessageToSend.MessageType.ENCRYPTED_TEXT) {
              messageToSend.setEncryptedMessage(
                  Security.encrypt(decryptedMessage, handler.user.getPublicKey()));
            }
            handler.outputStream.writeObject(messageToSend);
          }
        } catch (SocketException e) {
          System.err.println("message formation or handling error");
        }
      }
      messageToSend
          .setMessage("".equals(decryptedMessage) ? messageToSend.getMessage() : decryptedMessage);
    }
  }

  public int getRoomIndex() {
    return roomIndex;
  }

  public User getUser() {
    return user;
  }

}