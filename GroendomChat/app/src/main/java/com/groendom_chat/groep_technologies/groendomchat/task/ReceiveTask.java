package com.groendom_chat.groep_technologies.groendomchat.task;

import android.os.AsyncTask;
import com.groendom_chat.groep_technologies.ClientServer.Client.ClientFunctions;

/**
 * Created by Patrick Wissiak on 01.11.2017.
 */


public class ReceiveTask extends AsyncTask<Void, Void, Void> {

  private ClientFunctions functions;

  public ReceiveTask(ClientFunctions functions) {
    this.functions = functions;
  }

  @Override
  protected Void doInBackground(Void... params) {
    functions.waitForMessages();
    return null;
  }
}