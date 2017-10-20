package com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by tkr6u on 20.04.2017.
 * User specific to the client, used to save a custom name or a private key, should not be send!!
 */
public class ClientUser { //no longer extends User, so that it cannot be send!!
    private PrivateKey privateKey;
    private String customName;
    private User user;

    public ClientUser(User user) {
        this.user = user;
    }

    public ClientUser(String name, PublicKey publicKey, PrivateKey privateKey) {
        user = new User(name, publicKey);
        this.privateKey = privateKey;
    }
    public ClientUser(KeyPair pair){
        user = new User(pair);
        this.privateKey = pair.getPrivate();
        user.setName("test");
    }

    public String getOriginalName() {
        return user.getName();
    }


    public String getName() {
        if(customName == null){
            return user.getName();
        }else {
            return customName;
        }
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public User getUser(){
        return user;
    }

    /**
     *
     * @param oToCom needs to be instanceOf User
     * @return if the the public key are the same returns true
     */
    @Override
    public boolean equals(Object oToCom) {
        return (oToCom instanceof User && ((User) oToCom).getPublicKey().equals(user.getPublicKey())) ||
                (oToCom instanceof ClientUser && ((ClientUser) oToCom).getUser().getPublicKey().equals(user.getPublicKey()));
    }

}
