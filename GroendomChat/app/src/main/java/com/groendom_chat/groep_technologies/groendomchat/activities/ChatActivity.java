package com.groendom_chat.groep_technologies.groendomchat.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import com.groendom_chat.groep_technologies.groendomchat.R;
import com.groendom_chat.groep_technologies.groendomchat.model.Message;
import com.groendom_chat.groep_technologies.groendomchat.model.MessageHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("HIIOOO");

        MessageHandler messageHandler = new MessageHandler(getApplicationContext());
        LinearLayout layout = (LinearLayout) findViewById(R.id.chat_activity_content);


        for (int i = 1; i < 10; i++) {
            //Sent Message
            messageHandler.createOut(layout, new Message("Hallo " + i));

            //Incoming Message
            messageHandler.createIn(layout, new Message("Guten Morgen " + i));
        }


        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        messageHandler.createOut(layout, new Message("HIOOOooooooooooooooooooooooooooooooooooooo", cal.getTime()));
    }
}
