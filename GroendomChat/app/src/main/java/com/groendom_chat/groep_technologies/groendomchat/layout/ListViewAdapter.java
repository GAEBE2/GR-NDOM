package com.groendom_chat.groep_technologies.groendomchat.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.groendom_chat.groep_technologies.groendomchat.R;
import com.groendom_chat.groep_technologies.groendomchat.model.Message;

import java.util.ArrayList;

public class ListViewAdapter extends ArrayAdapter<Message> {
    private final LayoutInflater inflater;
    private final ArrayList<Message> values;

    public ListViewAdapter(Context context, ArrayList<Message> values) {
        super(context, -1, values);
        this.values = values;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = values.get(position);
        View rowView;
        if (message.isOutgoing()) {
            rowView = inflater.inflate(R.layout.bubble_out, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.bubble_in, parent, false);
        }

        ((TextView) rowView.findViewById(R.id.bubble_text_content)).setText(message.getContent());
        ((TextView) rowView.findViewById(R.id.bubble_text_date)).setText(message.getDateString());
        return rowView;
    }
}