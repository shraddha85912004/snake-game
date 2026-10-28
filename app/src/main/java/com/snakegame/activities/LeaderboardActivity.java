package com.snakegame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.snakegame.R;
import com.snakegame.data.HighScoreManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Leaderboard Activity
 * Displays all recorded player scores in descending order with ranking badges.
 */
public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Back button navigation
        Button btnBack = findViewById(R.id.btnBackLeaderboard);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        TextView tvContent = findViewById(R.id.tvLeaderboardContent);
        HighScoreManager hsm = new HighScoreManager(this);
        
        Map<String, Integer> scoresMap = hsm.getAllScores();
        List<Map.Entry<String, Integer>> list = new ArrayList<>(scoresMap.entrySet());
        
        // Sort descending by score
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : list) {
            String badge = "";
            if (rank == 1) badge = "🥇 ";
            else if (rank == 2) badge = "🥈 ";
            else if (rank == 3) badge = "🥉 ";
            else badge = "⭐ ";

            sb.append(badge)
              .append(rank).append(". ")
              .append(entry.getKey())
              .append("   •   ")
              .append(entry.getValue()).append(" pts\n\n");
            rank++;
        }

        if (sb.length() == 0) {
            tvContent.setText("No scores recorded yet.\nPlay a game to set the first record! 🎮");
        } else {
            tvContent.setText(sb.toString().trim());
        }
    }
}
