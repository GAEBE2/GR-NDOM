package com.groendom_chat.groep_technologies.ClientServer.Client;

import java.util.Objects;

/**
 * Created by P on 25.09.2017.
 */

public interface Consumer<T> {

    public void accept(T obj);
}