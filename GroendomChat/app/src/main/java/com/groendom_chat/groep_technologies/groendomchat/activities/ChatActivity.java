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
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.ClientServer.Operations.Security;
import com.groendom_chat.groep_technologies.groendomchat.R;
import com.groendom_chat.groep_technologies.groendomchat.layout.ListViewAdapter;
import com.groendom_chat.groep_technologies.groendomchat.task.ReceiveTask;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;

public class ChatActivity extends Activity {

  private final ArrayList<MessageToSend> items = new ArrayList<>();
  private ArrayAdapter<MessageToSend> itemsAdapter;
  private ClientFunctions functions;
  ClientUser clientUser = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_activity);

    final EditText editText = (EditText) findViewById(R.id.edit_text_message);
    final ListView listView = (ListView) findViewById(R.id.chat_activity_content);
    try {
      clientUser = new ClientUser(Security.generateKeyPair());

      itemsAdapter = new ListViewAdapter(this, items, clientUser.getUser());
      listView.setAdapter(itemsAdapter);

      functions = new ClientFunctions(new Consumer<MessageToSend>() {
        @Override
        public void accept(final MessageToSend message) {
          if (itemsAdapter != null) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                itemsAdapter.add(message);
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    listView.setSelection(itemsAdapter.getCount() - 1);
                  }
                });
              }
            });
          }
        }
      }, new Consumer<String>() {
        @Override
        public void accept(String obj) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(), "Other user left the chat room...",
                  Toast.LENGTH_LONG).show();
            }
          });
        }
      }, new Consumer<Integer>() {
        @Override
        public void accept(final Integer length) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
                  System.out.println("hoi:" + length);
              Toast.makeText(getApplicationContext(), "Number of members: " + length,
                  Toast.LENGTH_LONG).show();
            }
          });
        }
      });
      this.openChat();

      new ReceiveTask(functions).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      toolbar.setTitle("Chatroom");

      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          finish();
          functions.closeConnection();
        }
      });

    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      e.printStackTrace();
    }

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
              Toast.makeText(getApplicationContext(), "Failed to send message :(",
                  Toast.LENGTH_LONG).show();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }


  private void openChat() throws NoSuchProviderException, NoSuchAlgorithmException {
    functions.openConnection("192.168.0.71", clientUser);
  }

  protected void onStop() {
    super.onStop();
    functions.closeConnection();
  }
}
