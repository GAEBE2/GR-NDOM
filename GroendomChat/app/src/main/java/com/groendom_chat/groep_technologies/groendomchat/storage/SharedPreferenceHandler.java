package com.groendom_chat.groep_technologies.groendomchat.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Patrick Wissiak on 02.09.2017.
 */

public class SharedPreferenceHandler {

  private static final String PREFS = "speed-o-meter-preferences-file";
  private final SharedPreferences sharedPreferences;
  private Context context;

  public SharedPreferenceHandler(Context context) {
    this.context = context;
    // All objects are from android.context.Context
    this.sharedPreferences = context.getSharedPreferences(PREFS, 0);
  }

    /*public List<Float> getMeasurements(){
        int counter = sharedPreferences.getInt(COUNTER, 0);
        List<Float> list = new LinkedList();
        for (int i = 0; i < counter; i++) {
            list.add(sharedPreferences.getFloat(POINTS + Integer.toString(i), 0));
        }
        return list;
    }

    public void setLanguage(Language language) {
        this.setLocale(language.toString());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SETTINGS_LANGUAGE, language.toString());

        editor.apply();
    }*/
}
