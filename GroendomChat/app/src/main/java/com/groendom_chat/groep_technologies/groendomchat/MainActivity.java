package com.groendom_chat.groep_technologies.groendomchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.groendom_chat.groep_technologies.ClientServer.Client.Callback;
import com.groendom_chat.groep_technologies.ClientServer.Client.ClientFunctions;
import com.groendom_chat.groep_technologies.ClientServer.Client.Consumer;
import com.groendom_chat.groep_technologies.ClientServer.Client.SocketHandler;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;
import com.groendom_chat.groep_technologies.groendomchat.activities.ChatActivity;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private ClientFunctions functions;
  private ClientUser clientUser;
  private Socket socket;
  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

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
            public void onFinish(Object... param) {
              try {
                if (param != null && param[0] != null && param[0] instanceof Socket &&
                    param[1] != null && param[1] instanceof ObjectOutputStream &&
                    param[2] != null && param[2] instanceof ObjectInputStream) {

                  SocketHandler.setSocket((Socket) param[0]);
                  SocketHandler.setOutputStream((ObjectOutputStream) param[1]);
                  SocketHandler.setInputStream((ObjectInputStream) param[2]);

                  //Open connection was successful
                  Intent chatActivityIntent = new Intent(MainActivity.this, ChatActivity.class);
                  chatActivityIntent.putExtra(getString(R.string.client_user_value), clientUser);
                  startActivity(chatActivityIntent);
                } else {
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      //gets executed from the callback inside the AsyncTask
                      Toast.makeText(getApplicationContext(),
                          "Failed to open connection to the server", Toast.LENGTH_LONG).show();
                    }
                  });
                }
              } catch (ClassCastException e) {
                e.printStackTrace();
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
    functions.openConnection("10.4.57.128", clientUser, cb);
  }
}
