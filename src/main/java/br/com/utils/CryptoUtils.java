package br.com.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    // Cifra uma mensagem usando uma chave AES e IV aleatório
    public static String cifrarMensagem(String key, String message) {
        try {
            byte[] ivBytes = new byte[16];
            new SecureRandom().nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            // Configura o Cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            // Cifra a mensagem
            byte[] encrypted = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(ivBytes) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.out.println("Erro na criptografia: " + e.getMessage());
            return null;
        }
    }

    // Decifra uma mensagem, extraindo o IV do texto cifrado
    public static String decifrarMensagem(String key, String encryptedMessage) {
        try {
            // Divide o IV e o conteúdo cifrado
            String[] parts = encryptedMessage.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Mensagem cifrada inválida.");
            }
            byte[] ivBytes = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

            // Configura o Cipher
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            // Decifra a mensagem
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted);
        } catch (Exception e) {
            System.out.println("Erro na descriptografia: " + e.getMessage());
            return null;
        }
    }
}
