package com.andysierra.snakegame.views;

import android.graphics.drawable.Animatable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.andysierra.snakegame.R;
import com.andysierra.snakegame.activities.MainActivity;
import com.andysierra.snakegame.constants.Consts;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class Info implements Observer
{
    private static final String TAG="Info";
    private MainActivity mainActivity;

    public Info(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void updateTime(String time) {
        ((TextView)mainActivity.findViewById(R.id.txftiempo)).setText(time);
    }

    public void updateScore(int score, int highScore, String userName) {
        ((TextView)mainActivity.findViewById(R.id.txfManzanas)).setText(score + " (" + userName + "'s Best: " + highScore + ")");
    }

    @Override
    public void update(Observable o, Object arg) {
        int operation = ((int)((Object[])arg)[0]);
        final ImageView imgHint = (ImageView) this.mainActivity.findViewById(R.id.imghint);
        final TextView  txfHint = (TextView) this.mainActivity.findViewById(R.id.txfHint);

        if(operation == Consts.EXECUTE_UPDATE_SCORE) {
            int highScore = ((Object[])arg).length > 2 ? ((int)((Object[])arg)[2]) : 0;
            String userName = ((Object[])arg).length > 3 ? ((String)((Object[])arg)[3]) : "Player";
            this.updateScore(((int)((Object[])arg)[1]), highScore, userName);
        }

        else if(operation == Consts.EXECUTE_UPDATE_TIME_TXT)
            this.updateTime(((String)((Object[])arg)[1]));

        else if(operation == Consts.EXECUTE_UPDATE_HINT) {
            if(((String)((Object[])arg)[1]) == Consts.GOOD_BOMB) {
                txfHint.setText(Consts.GOOD_BOMB);
                imgHint.setImageDrawable(this.mainActivity.getDrawable(R.drawable.only_bomb));
            }
            if(((String)((Object[])arg)[1]) == Consts.BAD_BOMB) {
                txfHint.setText(Consts.BAD_BOMB);
                imgHint.setImageDrawable(this.mainActivity.getDrawable(R.drawable.only_bomb));
            }
            if(((String)((Object[])arg)[1]) == Consts.GAME_OVER) {
                txfHint.setText(Consts.GAME_OVER);
                imgHint.setImageDrawable(null);
            }


            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run() {
                    mainActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run() {
                            txfHint.setText("(ﾉ◕‿◕)ﾉ*:･ﾟ✧･ﾟ✧");
                            imgHint.setImageDrawable(null);
                        }
                    });
                }
            }, 7000);
        }
    }
}
