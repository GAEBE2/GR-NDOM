package com.groendom_chat.groep_technologies.groendomchat.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups.User;
import com.groendom_chat.groep_technologies.ClientServer.Operations.MessageToSend;
import com.groendom_chat.groep_technologies.groendomchat.R;

import java.util.ArrayList;

public class ListViewAdapter extends ArrayAdapter<MessageToSend> {
    private final LayoutInflater inflater;
    private ArrayList<MessageToSend> values;
    private User curUser;

    public ListViewAdapter(Context context, ArrayList<MessageToSend> values, User curUser) {
        super(context, -1, values);
        this.values = values;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.curUser = curUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageToSend message = values.get(position);
        View rowView;
        if (message.getAuthor().getUuid().equals(curUser.getUuid())) {
            rowView = inflater.inflate(R.layout.bubble_out, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.bubble_in, parent, false);
        }

        ((TextView) rowView.findViewById(R.id.bubble_text_content)).setText(message.getMessage());
        ((TextView) rowView.findViewById(R.id.bubble_text_date)).setText(message.getDateString());

        return rowView;
    }
}