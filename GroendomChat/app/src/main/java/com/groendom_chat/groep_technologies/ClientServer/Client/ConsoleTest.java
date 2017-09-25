package com.groendom_chat.groep_technologies.ClientServer.Client;

import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

public class ConsoleTest {
    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
        final ClientFunctions functions = new ClientFunctions(new Consumer<MessageToSend>() {
            @Override
            public void accept(MessageToSend obj) {
                System.out.println(obj.getMessage());
            }
        }, new Consumer<String>() {
            @Override
            public void accept(String obj) {
                System.out.println(obj);
            }
        }, new Consumer<List<ClientUser>>() {
            @Override
            public void accept(List<ClientUser> obj) {
                for(ClientUser user : obj) {
                    System.out.println(user);
                }
            }
        });
        functions.setActiveConsumers(new Consumer<MessageToSend>() {
            @Override
            public void accept(MessageToSend obj) {
                System.out.println(obj.getMessage());
            }
        });
        functions.openConnection("localhost", new ClientUser(Security.generateKeyPair()));
        //functions.sendMessage("hay§");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                functions.waitForMessages();
            }
        });
        thread.start();

        while (true){
            functions.sendMessage("test");
        }
    }
}