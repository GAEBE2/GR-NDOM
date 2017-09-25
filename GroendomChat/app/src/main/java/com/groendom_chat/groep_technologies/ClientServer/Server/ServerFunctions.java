import UserGroups.User;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by serge on 20-Mar-17.
 */
public class ServerFunctions {
    static List<Message> messageList = new LinkedList<>();
    static List<Handler> handlerList = new ArrayList<>();
    static List<User> userList = new ArrayList<>();
    static List<ChatRoom> roomList = new ArrayList<>();


    private static Logger LOG = Logger.getLogger("");
    static KeyPair pair;

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
                Handler handler = new Handler(listener.accept(), pair, roomList, userList, handler1 -> handlerList.remove(handler1));
                handlerList.add(handler);
                handler.start();
            }
        }
    }



    public static void log(String msg) {
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