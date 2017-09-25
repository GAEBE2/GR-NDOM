package com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by tkr6u on 20.04.2017.
 * User specific to the client, used to save a custom name or a private key, should not be send!!
 */
public class ClientUser extends User{
    private PrivateKey privateKey;
    private String customName;

    public ClientUser(User user) {
        super(user.getName(), user.getPublicKey());
    }

    public ClientUser(String name, PublicKey publicKey, PrivateKey privateKey) {
        super(name, publicKey);
        this.privateKey = privateKey;
    }
    public ClientUser(KeyPair pair){
        super(pair);
        this.privateKey = pair.getPrivate();
        setName("test");
    }

    public String getOriginalName() {
        return super.getName();
    }

    /**
     *
     * @return custom name if set
     */
    @Override
    public String getName() {
        if(customName == null){
            return super.getName();
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

    /**
     *
     * @param objectToCompare needs to be instanceOf User
     * @return if the the public key are the same returns true
     */
    @Override
    public boolean equals(Object objectToCompare) {
        boolean result = false;
        if(objectToCompare instanceof User && ((User) objectToCompare).getPublicKey().equals(this.getPublicKey())) {
            result = true;
        }
        return result;
    }

}
