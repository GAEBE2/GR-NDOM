package com.groendom_chat.groep_technologies.ClientServer.Server;

import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.User;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;


import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.net.ServerSocket;
import java.security.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by serge on 20-Mar-17.
 */
public class ServerFunctions {
    static List<MessageToSend> messageToSendList = new LinkedList<>();
    static List<Handler> handlerList = new ArrayList<>();
    static List<User> userList = new ArrayList<>();
    static List<ChatRoom> roomList = new ArrayList<>();


    private static Logger LOG = Logger.getLogger("");
    static KeyPair pair;

    private static boolean open = true;

    private static int port = MessageToSend.getPORT(); //standard

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
                Handler handler = new Handler(listener.accept(), pair, roomList, userList, handler1 -> handlerList.remove(handler1));
                handlerList.add(handler);
                handler.start();
            }
        }
    }

    public static int insetIntoRoom(Handler newHand){
        if(roomList == null){
            roomList = new ArrayList<>();
        }
        if(roomList.size() == 0 || !roomList.get(roomList.size() - 1).addHandler(newHand)){
            roomList.add(new ChatRoom(newHand));
        }
        return roomList.size() - 1;
    }

    static void log(String msg) {
        if(LOG == null){
            LOG =  Logger.getLogger("");
        }
        LOG.log(Level.ALL, msg);
        System.out.println(msg);
    }

    /**
     * generates a new keypair for the server
     */
    private static void generateNewKeysIfNecessary() {
        if(pair == null) {
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