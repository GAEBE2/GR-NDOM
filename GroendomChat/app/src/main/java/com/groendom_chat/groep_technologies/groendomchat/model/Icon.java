package com.groendom_chat.groep_technologies.groendomchat.model;

import com.groendom_chat.groep_technologies.groendomchat.R;

import java.util.HashMap;

/**
 * Created by Gabriel on 25.09.2017.
 */

public class Icon {

    private static final HashMap<String, Integer> icons;

    static {
        icons = new HashMap<>();
        icons.put("tear_laugh", R.drawable.tear_laugh_smiley);
        icons.put("tongue_wink", R.drawable.tongue_wink_smiley);
    }

    public Integer file;

    public Icon(String icon) {
        this.file = icons.get(icon);
    }
}
