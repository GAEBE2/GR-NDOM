package com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups;


import java.util.List;

/**
 * Created by tkr6u on 20.04.2017.
 */
public class Group {
    private String name;
    private List<User> serverUsers;

    public Group(String name, List<User> serverUsers) {
        this.name = name;
        this.serverUsers = serverUsers;
    }

    public boolean containsUser(User user) {
        return serverUsers.contains(user);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getServerUsers() {
        return serverUsers;
    }

    public void setServerUsers(List<User> serverUsers) {
        this.serverUsers = serverUsers;
    }
}
