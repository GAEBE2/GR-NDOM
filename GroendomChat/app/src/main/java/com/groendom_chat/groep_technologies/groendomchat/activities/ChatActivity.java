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
import android.widget.Toast;

import com.groendom_chat.groep_technologies.ClientServer.Client.ClientFunctions;
import com.groendom_chat.groep_technologies.ClientServer.Client.Consumer;
import com.groendom_chat.groep_technologies.ClientServer.Client.SocketHandler;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.groendomchat.MainActivity;
import com.groendom_chat.groep_technologies.groendomchat.R;
import com.groendom_chat.groep_technologies.groendomchat.layout.ListViewAdapter;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends Activity {
    private final ArrayList<MessageToSend> items = new ArrayList<>();
    private ArrayAdapter<MessageToSend> itemsAdapter;
    private ClientFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ClientUser clientUser = (ClientUser) getIntent().getSerializableExtra(getString(R.string.client_user_value));

        if (clientUser == null) {
            Toast.makeText(getApplicationContext(), "Failed pass data between the activities", Toast.LENGTH_LONG).show();
            finish();
        } else {
            functions = new ClientFunctions(new Consumer<MessageToSend>() {
                @Override
                public void accept(final MessageToSend message) {
                    if (itemsAdapter != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                itemsAdapter.add(message);
                            }
                        });
                    }
                }
            }, new Consumer<String>() {
                @Override
                public void accept(String obj) {
                    System.out.println("Removed user:" + obj);
                }
            }, new Consumer<List<ClientUser>>() {
                @Override
                public void accept(List<ClientUser> obj) {
                    for (ClientUser user : obj) {
                        System.out.println("Users:" + user);
                    }
                }
            });

            functions.setSocket(SocketHandler.getSocket());
            functions.setObjectInputStream(SocketHandler.getInputStream());
            functions.setObjectOutputStream(SocketHandler.getOutputStream());
            functions.setClientUser(clientUser);
            functions.setConnected(true);

            new ReceiveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

            itemsAdapter = new ListViewAdapter(this, items, clientUser.getUser());
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
                            if (functions.sendMessage(text)) {
                                //items.add(new MessageToSend(text, clientUser.getName()));
                                editText.setText("");

                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to send message :(", Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    protected void onStop() {
        super.onStop();
        functions.closeConnection();
    }

    private class ReceiveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            functions.waitForMessages();
            return null;
        }
    }
}
