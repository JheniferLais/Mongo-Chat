package org.example.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtils {
    private PasswordUtils() {}

    public static final String ALGO = "PBKDF2WithHmacSHA256";
    public static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    public static String generateSaltB64() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashToB64(String password, String saltB64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltB64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao hashear a senha", e);
        }
    }

    public static boolean verify(String password, String saltB64, String expectedHashB64) {
        byte[] expected = Base64.getDecoder().decode(expectedHashB64);
        byte[] computed = Base64.getDecoder().decode(hashToB64(password, saltB64));
        return MessageDigest.isEqual(computed, expected); // comparação em tempo constante
    }
}
