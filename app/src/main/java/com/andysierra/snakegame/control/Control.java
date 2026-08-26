package com.andysierra.snakegame.control;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import com.andysierra.snakegame.activities.MainActivity;
import com.andysierra.snakegame.constants.Consts;
import com.andysierra.snakegame.models.Game;
import com.andysierra.snakegame.models.Snake;
import com.andysierra.snakegame.views.Info;
import com.andysierra.snakegame.views.Board;

public class Control implements ViewTreeObserver.OnGlobalLayoutListener,    // Controlar el layout
                                View.OnTouchListener,                       // Controlar el touch
                                View.OnClickListener
{
    private static final String TAG="Control";
    private Board     board;
    private Info        info;
    private Game       game;
    private Snake[] snakes;
    private float   x1;
    private float   x2;
    private float   y1;
    private float   y2;



    // CONSTRUCTOR: Inicializar los objetos
    public Control(MainActivity mainActivity, Board board, Info info, Game game, Snake... snakes) {
        this.board    = board;
        this.info       = info;
        this.snakes = snakes;
        this.game      = game;

        try {
            if(snakes.length<1)
                throw new InstantiationException("Al instanciar un objeto control, debes pasar como " +
                        "parámetro al menos una snake");
        }
        catch (InstantiationException i) {
            Log.e(TAG, i.getMessage(), i);
            mainActivity.exitGame();
        }

        game.addObserver(board);
        game.addObserver(info);
        for(Snake s : snakes) s.addObserver(info);
        board.container.getViewTreeObserver().addOnGlobalLayoutListener(this);
        
        mainActivity.findViewById(com.andysierra.snakegame.R.id.btnUp).setOnClickListener(this);
        mainActivity.findViewById(com.andysierra.snakegame.R.id.btnDown).setOnClickListener(this);
        mainActivity.findViewById(com.andysierra.snakegame.R.id.btnLeft).setOnClickListener(this);
        mainActivity.findViewById(com.andysierra.snakegame.R.id.btnRight).setOnClickListener(this);
        mainActivity.findViewById(com.andysierra.snakegame.R.id.btnPause).setOnClickListener(this);
    }


    public void startGame() {
        this.game.prepareGame();

        // countdown
        new Thread(new Runnable()
        {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                int seconds = 3;
                while(true) {
                    if((System.currentTimeMillis()-time) > 1000) {
                        final String secStr = String.valueOf(seconds);
                        ((MainActivity)board.container.getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                board.message.setText(secStr);
                            }
                        });
                        time = System.currentTimeMillis();
                        if(seconds>0) seconds--; else break;
                    }
                }
                game.isPaused = false;
                ((MainActivity)board.container.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.message.setText("");
                    }
                });
            }
        }).start();
    }


    // LISTENER NECESARIO PARA SABER LAS DIMENSIONES DE MIS VISTAS Y PODER DIBUJAR EL JUEGO
    @Override
    public void onGlobalLayout() {
        board.container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        board.cellSize = board.container.getWidth() / Consts.COLS;
        this.startGame();
    }


    // LISTENER NECESARIO PARA EL TOUCH-SWIPE
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            x1 = event.getX();
            y1 = event.getY();
        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            x2 = event.getX();
            y2 = event.getY();

            float difX = x2-x1;
            float difY = y2-y1;
            if(Math.abs(difX) > Math.abs(difY))
                game.direction = (difX > 0)? Consts.direction.RIGHT : Consts.direction.LEFT;
            else
                game.direction = (difY > 0)? Consts.direction.DOWN : Consts.direction.UP;
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == com.andysierra.snakegame.R.id.btnUp) {
            game.direction = Consts.direction.UP;
        } else if (id == com.andysierra.snakegame.R.id.btnDown) {
            game.direction = Consts.direction.DOWN;
        } else if (id == com.andysierra.snakegame.R.id.btnLeft) {
            game.direction = Consts.direction.LEFT;
        } else if (id == com.andysierra.snakegame.R.id.btnRight) {
            game.direction = Consts.direction.RIGHT;
        } else if (id == com.andysierra.snakegame.R.id.btnPause) {
            game.isPaused = true;
            ((MainActivity)board.container.getContext()).showPauseDialog();
        }
    }
}
