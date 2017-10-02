package com.groendom_chat.groep_technologies.groendomchat.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.groendom_chat.groep_technologies.ClientServer.Client.ClientFunctions;
import com.groendom_chat.groep_technologies.ClientServer.Client.Consumer;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;
import com.groendom_chat.groep_technologies.groendomchat.R;
import com.groendom_chat.groep_technologies.groendomchat.model.Message;
import com.groendom_chat.groep_technologies.groendomchat.model.MessageHandler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends Activity {
    private ClientFunctions functions;
    private ClientUser clientUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        functions = new ClientFunctions(new Consumer<MessageToSend>() {
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
                for (ClientUser user : obj) {
                    System.out.println(user);
                }
            }
        });
        try {
            clientUser = new ClientUser(Security.generateKeyPair());
            this.openChat();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.chat_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("HIIOOO");

        final EditText editText = (EditText) findViewById(R.id.edit_text_message);
        MessageHandler messageHandler = new MessageHandler(getApplicationContext());
        LinearLayout layout = (LinearLayout) findViewById(R.id.chat_activity_content);
        FloatingActionButton sendButton = (FloatingActionButton) findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();
                if (!text.equals("")) {
                    try {
                        functions.sendMessage(text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        for (int i = 1; i < 10; i++) {
            //Sent Message
            messageHandler.createOut(layout, new Message("Hallo " + i));

            //Incoming Message
            messageHandler.createIn(layout, new Message("Guten Morgen " + i));
        }


        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        messageHandler.createOut(layout, new Message("HIOOOooooooooooooooooooooooooooooooooooooo", cal.getTime()));
    }

    private void openChat() throws NoSuchProviderException, NoSuchAlgorithmException {
        functions.setActiveConsumers(new Consumer<MessageToSend>() {
            @Override
            public void accept(MessageToSend obj) {
                System.out.println(obj.getMessage());
            }
        });
        functions.openConnection("192.168.0.71", clientUser);
        new ReceiveTask().execute();
    }

    private class ReceiveTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            functions.waitForMessages();
            return null;
        }

    }
}
