package com.groendom_chat.groep_technologies.ClientServer.Server;

import com.groendom_chat.groep_technologies.ClientServer.Client.Consumer;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.User;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by serge on 20-Mar-17.
 */
public class ServerFunctions {

  private static List<Handler> handlerList = new ArrayList<>();
  static List<User> userList = new ArrayList<>();
  static List<ChatRoom> roomList = new ArrayList<>();
  static KeyPair pair;
  private static Logger LOG = Logger.getLogger("");
  private static boolean open = true;

  private static int port = MessageToSend.getPORT(); //standard

  public static void main(String[] args) throws IOException {
    LOG.setLevel(Level.ALL);
    //set custom port and public/private key dir razin99

    for (String string : args) {
      if (NumberUtils.isParsable(string)) {
        port = Integer.parseInt(string);
      }
    }
    generateNewKeysIfNecessary();

    log("The chat server is running on port: " + port);
    try (ServerSocket listener = new ServerSocket(port)) {
      while (open) {
        Handler handler = new Handler(listener.accept(), pair, new Consumer<Handler>() {
          @Override
          public void accept(Handler handler) {
            handlerList.remove(handler);
          }
        });
        handlerList.add(handler);
        handler.start();
      }
    }
  }

  static int insetIntoRoom(Handler handler) {
    if (roomList == null) {
      roomList = new ArrayList<>();
    }
    int index = -1;
    for (int i = 0; i < roomList.size(); i++) {
      if (roomList.get(i).addHandler(handler)) {
        index = i;
        break;
      }
    }
    if (index == -1) {
      index = roomList.size();
      roomList.add(new ChatRoom(handler));
    }
    return index;
  }

  static int insertIntoNewRoom(Handler handler) {
    if (roomList == null) {
      roomList = new ArrayList<>();
    }
    int index = -1;
    for (int i = 0; i < roomList.size(); i++) {
      if (roomList.get(i).addHandler(handler)) {
        index = i;
        break;
      }
    }
    if (index == -1) {
      roomList.add(new ChatRoom(handler));
    }

    roomList.get(handler.getRoomIndex()).removeHandler(handler);
    return index;
  }

  static void log(String msg) {
    if (LOG == null) {
      LOG = Logger.getLogger("");
    }
    LOG.log(Level.ALL, msg);
    System.out.println(msg);
  }

  /**
   * generates a new keypair for the server
   */
  private static void generateNewKeysIfNecessary() {
    if (pair == null) {
      try {
        pair = Security.generateKeyPair();
      } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
        log("error generating new keyPair");
        e.printStackTrace();
        System.exit(-1);
      }
    }
  }
}