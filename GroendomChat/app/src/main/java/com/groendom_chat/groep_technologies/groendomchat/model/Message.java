package com.groendom_chat.groep_technologies.groendomchat.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by P on 18.09.2017.
 */

public class Message {
    private SimpleDateFormat dateFormat;
    private String content;
    private Date date;

    public Message(String content) {
        this(content, new Date(), null);
    }

    public Message(String content, Date date) {
        this(content, date, null);
    }

    public Message(String content, SimpleDateFormat dateFormat) {
        this(content, new Date(), dateFormat);
    }

    public Message(String content, Date date, SimpleDateFormat dateFormat) {
        this.setContent(content);
        this.setDate(date);
        this.setDateFormat(dateFormat);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        if(dateFormat == null) {
            if (date.before(getToday())) {
                return new SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(getDate());
            } else {
                return new SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(getDate());
            }
        } else {
            return dateFormat.format(date);
        }
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    private Date getToday() {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }
}
