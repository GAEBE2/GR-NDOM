package com.groendom_chat.groep_technologies.ClientServer.Client.UserGroups;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.UUID;

/**
 * Created by tkr6u on 21.04.2017.
 * a plain user object with only the public key
 */
public class User implements Serializable{

    private PublicKey publicKey;
    private String name;
    private UUID uuid;
    private int connection;

    public User(ClientUser user) {
        uuid = user.getUuid();
        publicKey = user.getPublicKey();
        connection = user.getConnection();
        name = user.getName();
    }

    public User(){
        uuid = UUID.randomUUID();
    }

    public User(KeyPair key){
        this();
        this.publicKey = key.getPublic();
    }

    public User(UUID uuid, PublicKey key){
        this.uuid = uuid;
        this.publicKey = key;
    }

    public User(String name) {
        this.name = name;
    }
    public User(String name, PublicKey key) {
        this.name = name;
        this.publicKey = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getConnection() {
        return connection;
    }

    public void setConnection(int connection) {
        this.connection = connection;
    }

    /*@Override
    public boolean equals(Object objectToCompare) {
        boolean result = false;
        if(objectToCompare instanceof User && ((User) objectToCompare).getPublicKey().equals(this.getPublicKey())) {
            result = true;
        }

        return result;
    }*/
}
