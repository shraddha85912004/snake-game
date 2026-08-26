package com.andysierra.snakegame.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.andysierra.snakegame.R;
import com.andysierra.snakegame.data.HighScoreManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        TextView tvContent = findViewById(R.id.tvLeaderboardContent);
        HighScoreManager hsm = new HighScoreManager(this);
        
        Map<String, Integer> scoresMap = hsm.getAllScores();
        List<Map.Entry<String, Integer>> list = new ArrayList<>(scoresMap.entrySet());
        
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : list) {
            sb.append(rank).append(". ").append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n\n");
            rank++;
        }

        if (sb.length() == 0) {
            tvContent.setText("No scores yet. Play a game!");
        } else {
            tvContent.setText(sb.toString());
        }
    }
}
