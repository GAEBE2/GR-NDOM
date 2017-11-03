package com.groendom_chat.groep_technologies.groendomchat.task;

/**
 * Created by Patrick Wissiak on 01.11.2017.
 */

import android.os.AsyncTask;
import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.ClientUser;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Task to send a message
 */
public class SendTask extends AsyncTask<String, Void, Boolean> {
  private ObjectOutputStream outputStream;
  private ClientUser clientUser;

  public SendTask(ObjectOutputStream outputStream, ClientUser clientUser) {
    this.outputStream = outputStream;
    this.clientUser = clientUser;
  }

  protected Boolean doInBackground(String... params) {
    for (String messageToSend : params) {
      try {
        //outputStream.writeObject(new MessageToSend(Security.encrypt(messageToSend, publicServerKey), clientUser.getName(), clientUser.getPublicKey()));
        outputStream
            .writeObject(MessageToSend.createTextMessage(messageToSend, clientUser.getUser()));
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
    return true;
  }
}
