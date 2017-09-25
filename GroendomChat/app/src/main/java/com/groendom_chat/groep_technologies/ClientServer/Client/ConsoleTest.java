import UserGroups.ClientUser;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.function.Consumer;

public class ConsoleTest {
    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
        ClientFunctions functions = new ClientFunctions(message -> System.out.println(message.getMessage()),
                System.out::println, clientUsers -> clientUsers.forEach(System.out::println));
        functions.setActiveConsumers(System.out::println);
        functions.openConnection("localhost", new ClientUser(Security.generateKeyPair()));
        //functions.sendMessage("hay§");
        Thread thread = new Thread(() -> functions.waitForMessages());
        thread.start();

        while (true){
            functions.sendMessage("test");
        }
    }
}
