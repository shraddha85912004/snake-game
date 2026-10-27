package com.snakegame.views;

import android.graphics.drawable.Animatable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.snakegame.R;
import com.snakegame.activities.MainActivity;
import com.snakegame.constants.Consts;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Info View (Observer)
 * Manages the Heads-Up Display (HUD) at the top of the game screen,
 * including the player's score, personal best, elapsed timer, and game hint messages.
 */
public class Info implements Observer
{
    private static final String TAG = "Info";
    private MainActivity mainActivity;

    // Constructor: attaches the reference to MainActivity for UI access
    public Info(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    // Updates the elapsed game time TextView
    public void updateTime(String time) {
        ((TextView)mainActivity.findViewById(R.id.txftiempo)).setText(time);
    }

    // Updates the score TextView with current score, username, and high score
    public void updateScore(int score, int highScore, String userName) {
        ((TextView)mainActivity.findViewById(R.id.txfManzanas))
            .setText(score + " (" + userName + "'s Best: " + highScore + ")");
    }

    /**
     * Observer update callback: triggers when the Game Model notifies of a state change.
     */
    @Override
    public void update(Observable o, Object arg) {
        int operation = ((int)((Object[])arg)[0]);
        final ImageView imgHint = (ImageView) this.mainActivity.findViewById(R.id.imghint);
        final TextView  txfHint = (TextView) this.mainActivity.findViewById(R.id.txfHint);

        // Update score and high score display
        if (operation == Consts.EXECUTE_UPDATE_SCORE) {
            int highScore = ((Object[])arg).length > 2 ? ((int)((Object[])arg)[2]) : 0;
            String userName = ((Object[])arg).length > 3 ? ((String)((Object[])arg)[3]) : "Player";
            this.updateScore(((int)((Object[])arg)[1]), highScore, userName);
        }

        // Update elapsed time text
        else if (operation == Consts.EXECUTE_UPDATE_TIME_TXT) {
            this.updateTime(((String)((Object[])arg)[1]));
        }

        // Display game hint messages (bomb alerts, game over, etc.)
        else if (operation == Consts.EXECUTE_UPDATE_HINT) {
            String hintMessage = (String)((Object[])arg)[1];

            if (Consts.GOOD_BOMB.equals(hintMessage)) {
                txfHint.setText(Consts.GOOD_BOMB);
                imgHint.setImageDrawable(this.mainActivity.getDrawable(R.drawable.only_bomb));
            }
            if (Consts.BAD_BOMB.equals(hintMessage)) {
                txfHint.setText(Consts.BAD_BOMB);
                imgHint.setImageDrawable(this.mainActivity.getDrawable(R.drawable.only_bomb));
            }
            if (Consts.GAME_OVER.equals(hintMessage)) {
                txfHint.setText(Consts.GAME_OVER);
                imgHint.setImageDrawable(null);
            }

            // Automatically clear the hint after 7 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txfHint.setText("");
                            imgHint.setImageDrawable(null);
                        }
                    });
                }
            }, 7000);
        }
    }
}
