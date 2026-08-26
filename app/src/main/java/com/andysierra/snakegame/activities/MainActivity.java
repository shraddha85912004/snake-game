package com.andysierra.snakegame.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.andysierra.snakegame.R;
import com.andysierra.snakegame.constants.Consts;
import com.andysierra.snakegame.control.Control;
import com.andysierra.snakegame.models.*;
import com.andysierra.snakegame.views.*;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG="MainActivity";
    Board             board;
    Info                info;
    Snake           snake;
    Game               game;
    Control             control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        board     = new Board((FrameLayout)findViewById(R.id.framelayout), this);
        info        = new Info(this);
        snake   = new Snake(Consts.INITIAL_POSITION_I,
                                    Consts.INITIAL_POSITION_J,
                                    Consts.INITIAL_LENGTH,
                                    Consts.INITIAL_ORIENTATION);
        game       = new Game(new WeakReference<MainActivity>(this), snake);
        
        String username = getIntent().getStringExtra("username");
        if (username != null && game.getHighScoreManager() != null) {
            game.getHighScoreManager().setCurrentUser(username);
        }
        
        control     = new Control(this, board, info, game, snake);
    }

    public void exitGame() {
        finishAndRemoveTask();
    }

    public void showPauseDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_pause);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.findViewById(R.id.btnHome).setOnClickListener(v -> {
            dialog.dismiss();
            exitGame();
        });

        dialog.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            dialog.dismiss();
            control.startGame();
        });

        dialog.findViewById(R.id.btnPlay).setOnClickListener(v -> {
            dialog.dismiss();
            game.isPaused = false;
        });

        dialog.show();
    }

    public void showGameOverDialog() {
        runOnUiThread(() -> {
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.setContentView(R.layout.dialog_game_over);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

            dialog.findViewById(R.id.btnReplay).setOnClickListener(v -> {
                dialog.dismiss();
                control.startGame();
            });

            dialog.findViewById(R.id.btnExit).setOnClickListener(v -> {
                dialog.dismiss();
                exitGame();
            });

            dialog.show();
        });
    }
}
