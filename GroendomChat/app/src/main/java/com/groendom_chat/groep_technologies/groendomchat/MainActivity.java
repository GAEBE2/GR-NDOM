package com.groendom_chat.groep_technologies.groendomchat;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.fab_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ChatActivity.class);
            }
        });
    }
}
