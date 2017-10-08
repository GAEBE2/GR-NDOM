package com.groendom_chat.groep_technologies.groendomchat.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.groendom_chat.groep_technologies.ClientServer.Client.ClientFunctions;
import com.groendom_chat.groep_technologies.ClientServer.Client.Consumer;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;
import com.groendom_chat.groep_technologies.groendomchat.R;
import com.groendom_chat.groep_technologies.groendomchat.layout.ListViewAdapter;
import com.groendom_chat.groep_technologies.groendomchat.model.Message;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
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
        toolbar.setTitle("Chat partner 1");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                functions.closeConnection();
            }
        });

        final ArrayList<Message> items = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            //Sent Message
            items.add(new Message("Hallo " + i));

            //Incoming Message
            items.add(new Message("Guten Morgen " + i, false));
        }
        final ArrayAdapter<Message> itemsAdapter = new ListViewAdapter(this, items);

        final EditText editText = (EditText) findViewById(R.id.edit_text_message);
        final ListView listView = (ListView) findViewById(R.id.chat_activity_content);
        listView.setAdapter(itemsAdapter);
        FloatingActionButton sendButton = (FloatingActionButton) findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();
                if (!text.equals("")) {
                    try {
                        functions.sendMessage(text);
                        items.add(new Message(text));
                        editText.setText("");
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                // Select the last row so it will scroll into view...
                                listView.setSelection(itemsAdapter.getCount() - 1);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(itemsAdapter.getCount() - 1);
            }
        });
    }

    protected void onStop() {
        super.onStop();
        functions.closeConnection();
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
