package com.snakegame.models;

import android.util.Log;
import com.snakegame.activities.MainActivity;
import com.snakegame.constants.Consts;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import com.snakegame.data.HighScoreManager;

/**
 * Game Model (Observable)
 * Central game engine managing the main game loop, timer, score,
 * speed scaling, item spawns (apples & bombs), and state broadcasts to Views.
 */
public class Game extends Observable
{
    private static final String TAG = "Game";
    private ArrayList<Observer> observers;
    WeakReference<MainActivity> weakActivity;
    private HighScoreManager highScoreManager;
    public static int[][] grid;
    private int randomSecond;
    public static int score,
                      seconds,
                      minutes,
                      hours,
                      applesInGame,
                      bombsInGame,
                      appleCounter,
                      poisonEffect;
    public boolean isPaused, isActive;
    private Thread loop;
    public Snake[] snakes;
    public Consts.direction direction;
    long snakeMovement; // Timestamp tracking snake movement intervals
    long time;          // Timestamp tracking 1-second elapsed clock intervals

    /**
     * Constructor: Initializes game settings, HighScoreManager, and registers Observers.
     */
    public Game(WeakReference<MainActivity> weakActivity, Snake... snakes) {
        this.weakActivity = weakActivity; // Weak reference to avoid Activity memory leaks
        if (weakActivity.get() != null) {
            this.highScoreManager = new HighScoreManager(weakActivity.get());
        }
        try {
            this.snakes = snakes;
            if (snakes.length < 1)
                throw new InstantiationException("When instantiating a game object, you must pass at least one snake as a parameter");
        }
        catch (InstantiationException i) {
            Log.e(TAG, i.getMessage(), i);
            ((MainActivity)weakActivity.get()).exitGame();
        }
        observers = new ArrayList<>();
        this.isPaused = true;
    }

    public HighScoreManager getHighScoreManager() {
        return this.highScoreManager;
    }

    /**
     * Dynamically calculates snake speed:
     * As the snake grows beyond the threshold, delay decreases by 5% per segment,
     * making the game progressively faster down to MIN_SNAKE_TIME.
     */
    public long getCurrentSnakeSpeed() {
        if (snakes != null && snakes.length > 0 && snakes[0] != null && snakes[0].body != null) {
            int currentSize = snakes[0].body.size() + 1; // Head + body segments
            if (currentSize > Consts.SIZE_THRESHOLD) {
                int excess = currentSize - Consts.SIZE_THRESHOLD;
                long speed = (long) (Consts.SNAKE_TIME * Math.pow(Consts.SPEED_DECREASE_FACTOR, excess));
                return Math.max(Consts.MIN_SNAKE_TIME, speed);
            }
        }
        return Consts.SNAKE_TIME;
    }

    /**
     * Prepares and resets the entire game board, score, timer, and snake.
     */
    public void prepareGame() {
        Game.grid = new int[Consts.ROWS][Consts.COLS];

        // Initialize checkered background pattern
        for (int i = 0; i < Consts.ROWS; i++) {
            for (int j = 0; j < Consts.COLS; j++) {
                Game.grid[i][j] = ((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)) ?
                        Consts.EMPTY1 : Consts.EMPTY2;
            }
        }

        // Reset game stats and counters
        Game.score = 0;
        Game.seconds = 0;
        Game.minutes = 0;
        Game.hours = 0;
        Game.applesInGame = 0;
        Game.bombsInGame = 0;
        Game.appleCounter = 0;
        Game.poisonEffect = 1;
        randomSecond = ((int)(Math.random() * 50 + 10));

        // Reset initial heading direction
        this.direction = Consts.direction.LEFT;

        // Reset snake model
        snakes[0].resetSnake();

        // Place initial snake head and segments on the grid
        Game.grid[snakes[0].headI][snakes[0].headJ] = Consts.SNAKE_HEAD;
        snakes[0].addSegments();

        // Start the game loop thread if activity is alive
        if (((MainActivity)weakActivity.get()) != null &&
            !((MainActivity)weakActivity.get()).isFinishing()) {
            this.looper();
        }
    }

    /**
     * Finds a random unoccupied cell on the board to spawn items.
     */
    private int[] getEmptyCell() {
        int i = (int)(Math.random() * Consts.ROWS);
        int j = (int)(Math.random() * Consts.COLS);

        while (Game.grid[i][j] != Consts.EMPTY1 && Game.grid[i][j] != Consts.EMPTY2) {
            i = (int)(Math.random() * Consts.ROWS);
            j = (int)(Math.random() * Consts.COLS);
        }
        return new int[]{i, j};
    }

    /**
     * Main game loop thread:
     * Advances snake movement based on dynamic speed and handles periodic 1-second events
     * (clock updates, warning countdowns, and item spawns).
     */
    private void looper() {
        isActive = true;
        isPaused = true;
        if (loop == null) {
            loop = new Thread(new Runnable() {
                @Override
                public void run() {
                    snakeMovement = System.currentTimeMillis();
                    time          = System.currentTimeMillis();

                    while (isActive) {
                        // Advance snake at current calculated speed interval
                        if (System.currentTimeMillis() - snakeMovement > getCurrentSnakeSpeed()) {
                            execute(Consts.EXECUTE_UP);           // Alternates item animation phase
                            execute(Consts.EXECUTE_UPDATE_BOARD); // Advances movement and redraws board
                            execute(Consts.EXECUTE_UPDATE_SCORE); // Updates live score display
                            snakeMovement = System.currentTimeMillis();
                        }

                        // Periodic 1-second tick (only when unpaused)
                        if (!isPaused) {
                            if (System.currentTimeMillis() - time > Consts.SECOND) {
                                if (!isPaused)
                                    execute(Consts.EXECUTE_TIMER);          // Increments elapsed seconds/minutes
                                execute(Consts.EXECUTE_UPDATE_TIME_TXT);    // Formats and displays clock text
                                execute(Consts.EXECUTE_UPDATE_MESSAGE);     // Checks for bomb warning broadcast
                                if (Game.applesInGame < 1)
                                    execute(Consts.EXECUTE_SPAWN_APPLE);    // Spawns new apple when board is clear
                                if (randomSecond == seconds)
                                    execute(Consts.EXECUTE_SPAWN_BOMB);     // Spawns periodic bomb obstacles
                                time = System.currentTimeMillis();
                            }
                        }
                    }
                }
            }, "game_loop_thread");
            loop.start();
        }
    }

    /**
     * Executes specified game operation and dispatches updates to Views on the main UI thread.
     */
    private synchronized void execute(int operation) {

        // Operation 1: Item bounce animation tick
        if (operation == Consts.EXECUTE_UP) {
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyObservers(new Object[]{ Consts.EXECUTE_UP });
                }
            });
        }

        // Operation 2: Snake movement and collision check
        if (operation == Consts.EXECUTE_UPDATE_BOARD) {
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Attempt step; if collision occurs, trigger Game Over
                    if (!isPaused) {
                        if (!snakes[0].move(direction)) {
                            Game.grid[snakes[0].headI][snakes[0].headJ] = Consts.SNAKE_HEAD_CRASH;
                            isPaused = true;
                            notifyObservers(new Object[]{
                                Consts.EXECUTE_UPDATE_HINT,
                                Consts.GAME_OVER
                            });
                            notifyObservers(new Object[]{
                                Consts.EXECUTE_UPDATE_MESSAGE,
                                "GAME OVER",
                                true
                            });
                            notifyObservers(new Object[]{
                                Consts.EXECUTE_UPDATE_TIME_TXT,
                                "00:00"
                            });
                            seconds = 0;
                        }
                    }

                    // Level progression check
                    if (snakes[0].nextLevel) {
                        prepareGame();
                    }

                    // Notify Board view to repaint current frame
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_BOARD,
                            grid,
                            snakes[0].body,
                            snakes[0].headDirection,
                            snakes[0].tailDirection,
                            snakes[0].headCollisionDirection
                    });
                }
            });
        }

        // Operation 3: Update score and persist high score
        else if (operation == Consts.EXECUTE_UPDATE_SCORE) {
            if (highScoreManager != null) {
                highScoreManager.saveHighScore(Game.score);
            }
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_SCORE,
                            Game.score,
                            highScoreManager != null ? highScoreManager.getHighScore() : 0,
                            highScoreManager != null ? highScoreManager.getCurrentUser() : "Player"
                    });
                }
            });
        }

        // Operation 4: Increment clock timer
        else if (operation == Consts.EXECUTE_TIMER) {
            seconds++;
            if (seconds > 59) {
                minutes++;
                seconds = 0;
                randomSecond = ((int)(Math.random() * 60));
            }
            if (minutes > 59) {
                hours++;
                minutes = 0;
            }
            if (hours > 24) hours = 0;
        }

        // Operation 5: Format and update elapsed time text (HH:MM:SS)
        else if (operation == Consts.EXECUTE_UPDATE_TIME_TXT) {
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_TIME_TXT,
                            (hours > 0) ?
                                    "" + hours + ":" + ((minutes < 10) ? "0" + minutes : minutes) + ":" + ((seconds < 10) ? "0" + seconds : seconds) :
                                    ((minutes < 10) ? "0" + minutes : minutes) + ":" + ((seconds < 10) ? "0" + seconds : seconds)
                    });
                }
            });
        }

        // Operation 6: Spawn apples (red standard apple, green bonus apple, or golden apple)
        else if (operation == Consts.EXECUTE_SPAWN_APPLE) {
            if (Game.score < Consts.NEXT_LEVEL) {
                if (poisonEffect > 1) poisonEffect--;

                if (seconds % poisonEffect == 0) {
                    int[] coords = this.getEmptyCell();
                    Game.grid[coords[0]][coords[1]] =
                            (appleCounter > 0 && appleCounter % 7 == 0) ?
                                    Consts.GREEN_APPLE : Consts.APPLE;
                    Game.applesInGame++;
                }
            }
            else {
                int[] coords = this.getEmptyCell();
                Game.grid[coords[0]][coords[1]] = Consts.GOLDEN_APPLE;
                Game.applesInGame++;
            }
        }

        // Operation 7: Spawn random bomb obstacles across the board
        else if (operation == Consts.EXECUTE_SPAWN_BOMB) {
            if (bombsInGame == 0) {
                for (int i = 0; i < (Consts.ROWS * Consts.COLS) / (10 - Consts.BOMB_DIFFICULTY); i++) {
                    int[] coords = this.getEmptyCell();
                    Game.grid[coords[0]][coords[1]] = Consts.BOMB;
                    bombsInGame++;
                }
            }

            // Remove bombs automatically after 5 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    bombsInGame = 0;
                    for (int i = 0; i < Consts.ROWS; i++) {
                        for (int j = 0; j < Consts.COLS; j++) {
                            if (grid[i][j] == Consts.BOMB) {
                                grid[i][j] = ((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)) ?
                                        Consts.EMPTY1 : Consts.EMPTY2;
                            }
                        }
                    }
                }
            }, 5000);
        }

        // Operation 8: Broadcast warning countdown message 4 seconds before bombs appear
        else if (operation == Consts.EXECUTE_UPDATE_MESSAGE) {
            if ((randomSecond - seconds) == 4) {
                ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyObservers(new Object[]{
                                Consts.EXECUTE_UPDATE_MESSAGE,
                                Consts.MSG_SPAWN_BOMB
                        });
                    }
                });
            }
        }
    }

    @Override
    public synchronized void addObserver(Observer o) { observers.add(o); }

    @Override
    public void notifyObservers(Object arg) { for (Observer o : observers) o.update(this, arg); }
}
