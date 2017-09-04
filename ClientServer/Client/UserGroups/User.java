package UserGroups;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Created by tkr6u on 21.04.2017.
 * a plain user object with only the public key
 */
public class User implements Serializable{

    private PublicKey publicKey;

    private String name;

    public User(){}

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

    @Override
    public boolean equals(Object objectToCompare) {
        boolean result = false;
        if(objectToCompare instanceof User && ((User) objectToCompare).getPublicKey().equals(this.getPublicKey())) {
            result = true;
        }

        return result;
    }
}
