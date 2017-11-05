package com.groendom_chat.groep_technologies.groendomchat.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

public class ChatActivity extends AppCompatActivity {

  private final ArrayList<MessageToSend> items = new ArrayList<>();
  private ArrayAdapter<MessageToSend> itemsAdapter;
  private ClientFunctions functions;
  ClientUser clientUser = null;
  private boolean connected;
  private boolean connectedWithUser;
  private EditText editText;
  private ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_activity);

    this.initToolbar();

    editText = (EditText) findViewById(R.id.edit_text_message);
    listView = (ListView) findViewById(R.id.chat_activity_content);
    try {
      clientUser = new ClientUser(Security.generateKeyPair());

      itemsAdapter = new ListViewAdapter(this, items, clientUser.getUser());
      listView.setAdapter(itemsAdapter);

      this.initClientFunctions();
      this.openChat();

      new ReceiveTask(functions).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      e.printStackTrace();
    }

    this.initButtons();
  }

  private void initClientFunctions() {
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
    }, new Consumer<Integer>() {
      @Override
      public void accept(Integer obj) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //itemsAdapter.clear();
            notConnectedWithUser();
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
            if(length == 2) {
              connectedWithUser = true;
              editText.setEnabled(true);
              editText.setHint(R.string.type_message);
            } else {
              notConnectedWithUser();
            }
            Toast.makeText(getApplicationContext(), "Number of members: " + length,
                Toast.LENGTH_LONG).show();
          }
        });
      }
    });
  }

  private void initButtons() {
    FloatingActionButton sendButton = (FloatingActionButton) findViewById(R.id.button_send);
    sendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String text = editText.getText().toString();
        if (connected && connectedWithUser && !text.equals("")) {
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
        } else if (!connected) {
          Toast.makeText(getApplicationContext(), "Not connected to the server",
              Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  private void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("Chatroom");

    setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
        functions.closeConnection();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.chat_activity_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.action_refresh:
        itemsAdapter.clear();
        functions.closeConnection();
        try {
          this.openChat();
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
          e.printStackTrace();
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void notConnectedWithUser() {
    connectedWithUser = false;
    editText.setEnabled(false);
    editText.setHint(R.string.not_connected_with_user);
  }


  private void openChat() throws NoSuchProviderException, NoSuchAlgorithmException {
    this.connected = functions.openConnection("192.168.0.71", clientUser);
    if (!connected) {
      Toast.makeText(getApplicationContext(), "Could not connected to the server",
          Toast.LENGTH_LONG).show();
    }
  }

  protected void onStop() {
    super.onStop();
    functions.closeConnection();
  }
}
