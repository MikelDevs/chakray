package com.techtest.api;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Encrypt {
    private static final String key = "q7Hk9vRbUeZm1APsLt3CXFnVYd2gBQKi";
    private static final String v = "W9pTz4MfEnKq3b0B";

    public static String encrypt(String value) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        IvParameterSpec ivParams = new IvParameterSpec(v.getBytes("UTF-8"));

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
        byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedValue) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        IvParameterSpec ivParams = new IvParameterSpec(v.getBytes("UTF-8"));

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));

        return new String(decrypted, "UTF-8");
    }
}
