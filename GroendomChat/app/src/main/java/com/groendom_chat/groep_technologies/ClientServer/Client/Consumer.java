package com.groendom_chat.groep_technologies.ClientServer.Client;

/**
 * Created by P on 25.09.2017.
 */

public interface Consumer<T> {

    /**
     * used as work around for functions as variables, because of java 7
     * @param obj
     */
    public void accept(T obj);
}
