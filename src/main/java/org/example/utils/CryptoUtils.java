package org.example.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtils {

    private static final String IV_PARAMETER = "0011223344556677"; // 16 bytes
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String cifrarMensagem(String key, String message) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.out.println("Erro na chave de criptografia");
            return null;
        }
    }

    public static String decifrarMensagem(String key, String encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decoded = Base64.getDecoder().decode(encryptedMessage);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            System.out.println("Erro na chave de descriptografia");
            return null;
        }
    }
}
