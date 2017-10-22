package com.groendom_chat.groep_technologies.groendomchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.groendom_chat.groep_technologies.ClientServer.Client.Callback;
import com.groendom_chat.groep_technologies.ClientServer.Client.ClientFunctions;
import com.groendom_chat.groep_technologies.ClientServer.Client.Consumer;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;
import com.groendom_chat.groep_technologies.groendomchat.activities.ChatActivity;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ClientFunctions functions;
    private ClientUser clientUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        functions = new ClientFunctions(null, new Consumer<String>() {
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

        findViewById(R.id.fab_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    clientUser = new ClientUser(Security.generateKeyPair());
                    openChat(new Callback() {
                        @Override
                        public void onFinish(Object param) {
                            if(param == null) {
                                //Open connection was successful
                                Intent chatActivityIntent = new Intent(MainActivity.this, ChatActivity.class);
                                chatActivityIntent.putExtra(getString(R.string.client_functions_value), functions);
                                startActivity(chatActivityIntent);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //gets executed from the callback inside the AsyncTask
                                        Toast.makeText(getApplicationContext(), "Failed to open connection to the server", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void openChat(Callback cb) throws NoSuchProviderException, NoSuchAlgorithmException {
        functions.openConnection("192.168.0.71", clientUser, cb);
    }
}
