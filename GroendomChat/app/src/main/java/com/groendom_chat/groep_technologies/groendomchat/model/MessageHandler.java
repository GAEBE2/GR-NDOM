package com.groendom_chat.groep_technologies.groendomchat.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groendom_chat.groep_technologies.groendomchat.R;

/**
 * Created by P on 18.09.2017.
 */

public class MessageHandler {
    private Context context;

    public MessageHandler(Context context) {
        this.context = context;
    }

    public void createOut(ViewGroup parent, Message message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout child = (RelativeLayout) inflater.inflate(R.layout.bubble_out, parent, false);

        ((TextView) child.findViewById(R.id.bubble_text_content)).setText(message.getContent());
        ((TextView) child.findViewById(R.id.bubble_text_date)).setText(message.getDateString());
        parent.addView(child);
    }

    public void createIn(ViewGroup parent, Message message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout child = (RelativeLayout) inflater.inflate(R.layout.bubble_in, parent, false);

        ((TextView) child.findViewById(R.id.bubble_text_content)).setText(message.getContent());
        ((TextView) child.findViewById(R.id.bubble_text_date)).setText(message.getDateString());
        parent.addView(child);
    }
}
