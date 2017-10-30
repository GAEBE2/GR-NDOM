package com.groendom_chat.groep_technologies.groendomchat.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.groendomchat.R;

/**
 * Created by P on 18.09.2017.
 */

public class MessageHandler {

  private Context context;

  public MessageHandler(Context context) {
    this.context = context;
  }

  public void createOut(ViewGroup parent, MessageToSend message) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    RelativeLayout child = (RelativeLayout) inflater.inflate(R.layout.bubble_out, parent, false);

    ((TextView) child.findViewById(R.id.bubble_text_content)).setText(message.getMessage());
    ((TextView) child.findViewById(R.id.bubble_text_date)).setText(message.getDateString());
    parent.addView(child);
  }

  public void createOut(ViewGroup parent, Icon icon) {

  }
}
