package com.snakegame.control;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import com.snakegame.activities.MainActivity;
import com.snakegame.constants.Consts;
import com.snakegame.models.Game;
import com.snakegame.models.Snake;
import com.snakegame.views.Info;
import com.snakegame.views.Board;

/**
 * Controller class in the MVC architecture.
 * Mediates between user input (D-Pad clicks & swipe gestures),
 * the Game Model (state, movement, pauses), and the Views (Board, Info).
 */
public class Control implements ViewTreeObserver.OnGlobalLayoutListener, // Calculates board dimensions on layout load
                                View.OnTouchListener,                    // Handles swipe touch events (fallback)
                                View.OnClickListener                     // Handles D-Pad button clicks
{
    private static final String TAG = "Control";
    private Board board;
    private Info info;
    private Game game;
    private Snake[] snakes;
    private float x1, x2;
    private float y1, y2;

    /**
     * Constructor: Initializes game components, links Observers to Models,
     * and attaches click listeners to on-screen D-Pad and pause controls.
     */
    public Control(MainActivity mainActivity, Board board, Info info, Game game, Snake... snakes) {
        this.board  = board;
        this.info   = info;
        this.snakes = snakes;
        this.game   = game;

        try {
            if (snakes.length < 1)
                throw new InstantiationException("When instantiating a control object, you must pass at least one snake as a parameter");
        }
        catch (InstantiationException i) {
            Log.e(TAG, i.getMessage(), i);
            mainActivity.exitGame();
        }

        // Register Views as Observers of the Game Model
        game.addObserver(board);
        game.addObserver(info);
        for (Snake s : snakes) s.addObserver(info);

        // Listen for layout completion to calculate exact cell sizes
        board.container.getViewTreeObserver().addOnGlobalLayoutListener(this);
        
        // Wire up on-screen D-Pad and Pause button click listeners
        mainActivity.findViewById(com.snakegame.R.id.btnUp).setOnClickListener(this);
        mainActivity.findViewById(com.snakegame.R.id.btnDown).setOnClickListener(this);
        mainActivity.findViewById(com.snakegame.R.id.btnLeft).setOnClickListener(this);
        mainActivity.findViewById(com.snakegame.R.id.btnRight).setOnClickListener(this);
        mainActivity.findViewById(com.snakegame.R.id.btnPause).setOnClickListener(this);
    }

    /**
     * Resets and starts the game with a 3-2-1 countdown overlay.
     * The game loop unpauses once the countdown hits zero.
     */
    public void startGame() {
        this.game.prepareGame();

        // Start 3-second visual countdown in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                int seconds = 3;
                while (true) {
                    if ((System.currentTimeMillis() - time) > 1000) {
                        final String secStr = String.valueOf(seconds);
                        ((MainActivity)board.container.getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                board.message.setText(secStr);
                            }
                        });
                        time = System.currentTimeMillis();
                        if (seconds > 0) seconds--; else break;
                    }
                }
                // Unpause the game loop once the countdown finishes
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

    /**
     * Global layout listener:
     * Triggered when the view hierarchy is measured. Calculates the exact cellSize
     * based on available container width divided by grid columns, ensuring responsive scaling.
     */
    @Override
    public void onGlobalLayout() {
        board.container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        board.cellSize = board.container.getWidth() / Consts.COLS;
        this.startGame();
    }

    /**
     * Touch listener:
     * Detects swipe gestures across the screen by comparing touch start and end coordinates.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();

            float difX = x2 - x1;
            float difY = y2 - y1;
            // Determine whether horizontal or vertical swipe had greater displacement
            if (Math.abs(difX) > Math.abs(difY))
                game.direction = (difX > 0) ? Consts.direction.RIGHT : Consts.direction.LEFT;
            else
                game.direction = (difY > 0) ? Consts.direction.DOWN : Consts.direction.UP;
        }
        return true;
    }

    /**
     * Click listener:
     * Handles D-Pad directional button presses (Up, Down, Left, Right)
     * and the Pause button to trigger the custom Pause dialog.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == com.snakegame.R.id.btnUp) {
            game.direction = Consts.direction.UP;
        } else if (id == com.snakegame.R.id.btnDown) {
            game.direction = Consts.direction.DOWN;
        } else if (id == com.snakegame.R.id.btnLeft) {
            game.direction = Consts.direction.LEFT;
        } else if (id == com.snakegame.R.id.btnRight) {
            game.direction = Consts.direction.RIGHT;
        } else if (id == com.snakegame.R.id.btnPause) {
            game.isPaused = true;
            ((MainActivity)board.container.getContext()).showPauseDialog();
        }
    }
}
