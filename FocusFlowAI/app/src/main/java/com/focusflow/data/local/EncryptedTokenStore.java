package com.focusflow.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

/**
 * JWT storage backed by Android Keystore via EncryptedSharedPreferences.
 *
 * Tokens deliberately fail closed if the Android Keystore is unavailable: storing an
 * access token in regular preferences would turn a device security failure into a
 * silent security downgrade.
 */
public final class EncryptedTokenStore {

    private static final String SECURE_FILE_NAME = "focusflow_secure";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    @Nullable
    private final SharedPreferences securePreferences;
    private final SharedPreferences legacyPreferences;

    public EncryptedTokenStore(Context context, SharedPreferences legacyPreferences) {
        this.legacyPreferences = legacyPreferences;
        this.securePreferences = createSecurePreferences(context);
    }

    @Nullable
    private static SharedPreferences createSecurePreferences(Context context) {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    SECURE_FILE_NAME,
                    masterKey,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    public String accessToken() {
        migrateLegacyTokens();
        return securePreferences == null ? "" : emptyToEmpty(securePreferences.getString(KEY_ACCESS_TOKEN, ""));
    }

    public String refreshToken() {
        migrateLegacyTokens();
        return securePreferences == null ? "" : emptyToEmpty(securePreferences.getString(KEY_REFRESH_TOKEN, ""));
    }

    public void setTokens(String access, String refresh) {
        if (securePreferences == null) {
            clearLegacyTokens();
            return;
        }
        securePreferences.edit()
                .putString(KEY_ACCESS_TOKEN, access == null ? "" : access)
                .putString(KEY_REFRESH_TOKEN, refresh == null ? "" : refresh)
                .apply();
        clearLegacyTokens();
    }

    public boolean isAvailable() {
        return securePreferences != null;
    }

    private void migrateLegacyTokens() {
        if (securePreferences == null) {
            clearLegacyTokens();
            return;
        }
        if (!legacyPreferences.contains(KEY_ACCESS_TOKEN) && !legacyPreferences.contains(KEY_REFRESH_TOKEN)) {
            return;
        }
        if (!isEmpty(securePreferences.getString(KEY_ACCESS_TOKEN, ""))) {
            clearLegacyTokens();
            return;
        }
        String access = legacyPreferences.getString(KEY_ACCESS_TOKEN, "");
        String refresh = legacyPreferences.getString(KEY_REFRESH_TOKEN, "");
        securePreferences.edit()
                .putString(KEY_ACCESS_TOKEN, access == null ? "" : access)
                .putString(KEY_REFRESH_TOKEN, refresh == null ? "" : refresh)
                .apply();
        clearLegacyTokens();
    }

    private void clearLegacyTokens() {
        if (legacyPreferences.contains(KEY_ACCESS_TOKEN) || legacyPreferences.contains(KEY_REFRESH_TOKEN)) {
            legacyPreferences.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).apply();
        }
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private static String emptyToEmpty(String value) {
        return value == null ? "" : value;
    }
}
