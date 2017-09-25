package com.groendom_chat.groep_technologies.ClientServer.Operations;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;

/**
 * Created by tkr6u on 20.04.2017.
 */
public class Security {

    private static SecureRandom random = new SecureRandom();
    private static KeyFactory factory = getNewFactory();
    private static KeyPairGenerator keyPairGenerator;

    /**
     *
     * @return a new Factory should only be used to initialize the variable
     */
    private static KeyFactory getNewFactory() {
        try {
            return KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @return a new KeyPair with the factory method defined above
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        keyPairGenerator = keyPairGenerator == null ? KeyPairGenerator.getInstance("RSA") : keyPairGenerator;
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.genKeyPair();
    }

    public static  Cipher getSingingCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("RSA");
    }

    public static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("RSA");///ECB/PKCS1Padding
    }

    /**
     *
     * @param text message to encrypt
     * @param key key to be used
     * @return encrypted message
     */
    public static byte[] encrypt(String text, Key key) {
        byte[] cipherText = null;
        try {
            Cipher cipher = key instanceof PrivateKey ? getSingingCipher() : getCipher();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     *
     * @param encryptedText text that has been encrypted
     * @param key to use to decrypt encryptedText
     * @return decrypted text
     */
    public static String decrypt(byte[] encryptedText, Key key) {
        byte[] decryptedText = null;
        try {
            Cipher cipher = key instanceof PublicKey ? getSingingCipher() :  getCipher();
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(encryptedText);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return decryptedText != null ? new String(decryptedText) : null;
    }

    /**
     * uses secure random
     * @return to generate a random string, should be used for authentication
     */
    public static String randomlyGenerateAString() {
        return new BigInteger(130, random).toString(32);
    }

    public static Key convertStringToKey(String string, boolean isPublic) {
        byte[] encodedKey;
        try {
            encodedKey = Base64.decode(string, Base64.DEFAULT);
            return isPublic ? factory.generatePublic(new X509EncodedKeySpec(encodedKey)) : factory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * should be used if one wants to handle exception thyself
     * @param string key
     * @param isPublic whether it's a public or private key
     * @return key
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public static Key convertStringToKeyWithException(String string, boolean isPublic) throws IOException, InvalidKeySpecException {
        byte[] encodedKey;
        encodedKey = Base64.decode(string, Base64.DEFAULT);
        return isPublic ? factory.generatePublic(new X509EncodedKeySpec(encodedKey)) : factory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    /*
    /**
     *
     * @param key key one wants to convert
     * @return converted key
     */
    /*
    public static String convertKeyToString(Key key) {
        try {
            EncodedKeySpec spec = key instanceof PublicKey ? factory.getKeySpec(key, X509EncodedKeySpec.class) : factory.getKeySpec(key, PKCS8EncodedKeySpec.class);
            return Base64.encode(spec.getEncoded(), Base64.DEFAULT);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
         return encoder.encode(key.getEncoded());
    }
    */
    //TODO: add message digest for more security

    public static KeyFactory getFactory() {
        return factory;
    }
}