package com.zybooks.inventorytracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/* TokenManager stores the JWT on disk, but encrypted. The encryption key is generated
 * and stored inside Android Keystore so that the key is never present in code */
public class TokenManager {

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "auth_token_key";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String IV_KEY = "auth_token_iv";
    private static final int GCM_TAG_LENGTH = 128;

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        try {
            SecretKey key = getOrCreateKey();

            // Sets cipher algorithm, block mode, and padding scheme
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Initialization vector
            byte[] iv = cipher.getIV();

            // Encrypts the token's raw bytes
            byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));

            // Converted to string as SharedPrefs can only stores strings, not raw bytes
            prefs.edit()
                    .putString(TOKEN_KEY, Base64.encodeToString(encrypted, Base64.DEFAULT))
                    .putString(IV_KEY, Base64.encodeToString(iv, Base64.DEFAULT))
                    .apply();

        } catch (Exception e) {
            throw new RuntimeException("Failed to save token", e);
        }
    }

    /* Reads and decrypts the saved token.
     * Returns null if no token is saved, or if decryption fails */
    public String getToken() {
        String encryptedBase64 = prefs.getString(TOKEN_KEY, null);
        String ivBase64 = prefs.getString(IV_KEY, null);

        if (encryptedBase64 == null || ivBase64 == null) {
            return null;
        }

        try {
            SecretKey key = getOrCreateKey();

            // Convert Base64 text back into raw bytes
            byte[] iv = Base64.decode(ivBase64, Base64.DEFAULT);
            byte[] encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT);

            // Sets algorithm, blocking, and padding scheme
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // Decrypts the token using the same stored IV that was used to encrypt it
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            // Decryption failure - Treat as logged out
            return null;
        }
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void logout() {
        prefs.edit()
                .remove(TOKEN_KEY)
                .remove(IV_KEY)
                .apply();
    }

    /* Fetches AES key from Android Keystore, creating it first time this runs.
     * After that, same key is reused every time.  */
    private SecretKey getOrCreateKey() throws Exception {
        // Load a handle to the Android Keystore
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);

        // If key exists, fetch and reuse it
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        }

        // Otherwise, this is first time -  Generate a new AES key,
        // and tell Keystore to store it under alias
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER
        );

        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build();

        keyGenerator.init(spec);
        return keyGenerator.generateKey();
    }
}