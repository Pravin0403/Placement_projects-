package com.focusflow.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class OnboardingPreferences {

    private static final String FILE_NAME = "focusflow_onboarding";
    private static final String KEY_COMPLETED = "onboarding_completed";

    private final SharedPreferences preferences;

    public OnboardingPreferences(Context context) {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isCompleted() {
        return preferences.getBoolean(KEY_COMPLETED, false);
    }

    public void setCompleted(boolean completed) {
        preferences.edit().putBoolean(KEY_COMPLETED, completed).commit();
    }
}
