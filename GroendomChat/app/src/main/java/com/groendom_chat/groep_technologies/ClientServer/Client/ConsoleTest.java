package com.groendom_chat.groep_technologies.ClientServer.Client;

import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class ConsoleTest {
    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InterruptedException {
        ClientFunctions functions = new ClientFunctions(message -> System.out.println(message.getMessage()),
                System.out::println, clientUsers -> clientUsers.forEach(System.out::println));
        functions.setActiveConsumers(System.out::println);
        functions.openConnection("localhost", new ClientUser(Security.generateKeyPair()));
        //functions.sendMessage("hay§");
        Thread thread = new Thread(functions::waitForMessages);
        thread.start();
        for (int i = 0; i < 1000; i++) {
            System.out.println(i);
            functions.sendMessage("test");
            Thread.sleep(500);
        }
    }
}
