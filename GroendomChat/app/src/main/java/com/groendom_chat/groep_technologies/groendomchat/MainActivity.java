package com.groendom_chat.groep_technologies.groendomchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.groendom_chat.groep_technologies.groendomchat.activities.ChatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.fab_start).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ChatActivity.class)));
    }
}
