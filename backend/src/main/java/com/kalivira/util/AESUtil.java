package com.kalivira.util;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.security.MessageDigest;

public class AESUtil {

    //converting password in aes key
    public static SecretKey generateKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");//32 bytes
        byte[] key = digest.digest(password.getBytes("UTF-8"));
        key = Arrays.copyOf(key, 16);//16byte = 128 bits
        return new SecretKeySpec(key, "AES");
    }

    //encrypt file data
    public static byte[] encrypt(byte[] data,String password) throws Exception {
        SecretKey secretKey = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);

    }
}
