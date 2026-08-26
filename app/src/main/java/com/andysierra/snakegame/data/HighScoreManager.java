package com.andysierra.snakegame.data;

import android.content.Context;
import android.content.SharedPreferences;

public class HighScoreManager {
    private static final String PREF_NAME = "SnakeGamePrefs";
    private static final String KEY_PREFIX = "HighScore_";
    private SharedPreferences sharedPreferences;
    private String currentUser = "Player 1";

    public HighScoreManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setCurrentUser(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.currentUser = name.trim();
        }
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public int getHighScore() {
        return sharedPreferences.getInt(KEY_PREFIX + currentUser, 0);
    }

    public void saveHighScore(int score) {
        int currentHighScore = getHighScore();
        if (score > currentHighScore) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_PREFIX + currentUser, score);
            editor.apply();
        }
    }

    public java.util.Map<String, Integer> getAllScores() {
        java.util.Map<String, Integer> scores = new java.util.HashMap<>();
        java.util.Map<String, ?> allEntries = sharedPreferences.getAll();
        for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith(KEY_PREFIX)) {
                String name = entry.getKey().replace(KEY_PREFIX, "");
                if (entry.getValue() instanceof Integer) {
                    scores.put(name, (Integer) entry.getValue());
                }
            }
        }
        return scores;
    }
}
