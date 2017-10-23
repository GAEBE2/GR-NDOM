package com.groendom_chat.groep_technologies.ClientServer.Operations;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Created by serge on 23-Apr-17.
 * Used for the quite authentication that happens when a client tries to connect to a server
 */
public class Authentication implements Serializable {
    private byte[] publicKey;
    private String originalMessage;
    private byte[] encryptedMessage;

    public Authentication(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey.getEncoded();
    }

    public void setEncryptedMessage(byte[] encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public byte[] getEncryptedMessage() {
        return encryptedMessage;
    }
}