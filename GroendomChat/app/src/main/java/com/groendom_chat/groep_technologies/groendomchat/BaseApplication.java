package com.groendom_chat.groep_technologies.groendomchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by P on 28.08.2017.
 */

public class BaseApplication extends AppCompatActivity {
    public void startActivity(Class<?> newActivity) {
        startActivity(new Intent(this, newActivity));
    }
}
