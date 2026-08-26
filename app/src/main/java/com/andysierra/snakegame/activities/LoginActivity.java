package com.andysierra.snakegame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.andysierra.snakegame.R;
import com.andysierra.snakegame.data.HighScoreManager;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText editUsername = findViewById(R.id.editUsername);
        Button btnStartGame = findViewById(R.id.btnStartGame);

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editUsername.getText().toString().trim();
                if (name.isEmpty()) {
                    name = "Player 1";
                }
                
                // Save name to HighScoreManager before starting game
                HighScoreManager hsm = new HighScoreManager(LoginActivity.this);
                hsm.setCurrentUser(name);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("username", name);
                startActivity(intent);
                finish();
            }
        });

        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, LeaderboardActivity.class);
                startActivity(intent);
            }
        });
    }
}
