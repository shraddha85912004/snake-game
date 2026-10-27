package com.snakegame.views;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.snakegame.R;
import com.snakegame.activities.MainActivity;
import com.snakegame.constants.Consts;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Board View (Observer)
 * Handles rendering of the entire game arena onto an Android GridLayout.
 * Dynamically draws the checkered background, snake body/head/tail/corners,
 * apples, bombs, and overlay messages (countdown warnings and game over dialog).
 */
public class Board <T extends ViewGroup> implements Observer
{
    private static final String TAG = "Board";
    private static int rotation = 0;
    public GridLayout gridLayout;
    public LinearLayout messageLayout;
    public TextView message;
    public Button btnRestart, btnExit;
    public static String hint;
    private int[][] grid;
    private ArrayList<int[]> rotatableCells;
    private Consts.direction headDirection, collisionDirection, tailDirection;
    private boolean up;
    public T container;
    private MainActivity mainActivity;
    public int cellSize;

    // Constructor: initializes view components and builds the board layout
    public Board(T container, MainActivity mainActivity) {
        this.up             = true;
        this.container      = container;
        this.mainActivity   = mainActivity;
        gridLayout          = new GridLayout(this.mainActivity);
        messageLayout       = new LinearLayout(mainActivity);
        message             = new TextView(mainActivity);
        btnRestart          = new Button(mainActivity);
        btnExit             = new Button(mainActivity);
        grid                = null;
        rotatableCells      = null;
        headDirection       = null;
        this.prepare();
    }

    // Configures and mounts the grid layout and message overlay container
    private void prepare() {
        gridLayout.setColumnCount(Consts.COLS);
        gridLayout.setRowCount(Consts.ROWS);
        messageLayout.setBackgroundColor(Color.TRANSPARENT);
        messageLayout.setGravity(Gravity.CENTER);
        messageLayout.setOrientation(LinearLayout.VERTICAL);

        message.setText("");
        message.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        message.setTextSize(40);
        message.setGravity(Gravity.CENTER);
        message.setTypeface(null, Typeface.BOLD);
        message.setTextColor(Color.WHITE);

        btnRestart.setText("Play Again");
        btnRestart.setVisibility(View.INVISIBLE);
        btnRestart.setTag("btnRestart");

        btnExit.setText("Exit");
        btnExit.setTag("btnExit");
        btnExit.setVisibility(View.INVISIBLE);

        container.addView(gridLayout);
        container.addView(messageLayout);
        messageLayout.addView(message);
        messageLayout.addView(btnRestart);
        messageLayout.addView(btnExit);
        messageLayout.getLayoutParams().height = 1000;
    }

    // Assigns click listeners to overlay buttons from the controller
    public void setOnClickListener(View.OnClickListener control) {
        this.btnRestart.setOnClickListener(control);
        this.btnExit.setOnClickListener(control);
    }

    /**
     * Observer update callback: redraws the board whenever the Game Model changes.
     */
    @Override
    public void update(Observable o, Object arg) {

        // Operation 1: Redraw all cells on the board based on the grid matrix
        if (((int)((Object[])arg)[0]) == Consts.EXECUTE_UPDATE_BOARD) {
            // Remove previous frame's cells
            if (gridLayout.getChildCount() > 0) gridLayout.removeAllViews();

            this.grid               = ((int[][])((Object[])arg)[1]);
            this.rotatableCells     = ((ArrayList<int[]>)((Object[])arg)[2]);
            this.headDirection      = ((Consts.direction)((Object[])arg)[3]);
            this.tailDirection      = ((Consts.direction)((Object[])arg)[4]);
            this.collisionDirection = ((Consts.direction)((Object[])arg)[5]);

            ImageView cell;

            // Iterate through every row and column of the grid
            for (int i = 0; i < Consts.ROWS; i++) {
                for (int j = 0; j < Consts.COLS; j++) {

                    // Create an ImageView for the current cell
                    cell = new ImageView(mainActivity);
                    gridLayout.addView(cell);

                    // Alternating pattern for the checkered background effect
                    boolean isEvenCell = (i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0);

                    switch (grid[i][j]) {
                        case Consts.EMPTY1:
                            cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_greenie));
                            break;

                        case Consts.EMPTY2:
                            cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_greenie_alt));
                            break;

                        case Consts.SNAKE:
                            // Straight or curved snake body segment
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_alt));
                            this.rotateSegment(i, j, cell);
                            break;

                        case Consts.SNAKE_HEAD:
                            // Snake head facing current movement direction
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza_alt));
                            this.rotateSegment(cell, true);
                            break;

                        case Consts.SNAKE_TAIL:
                            // Snake tail trailing behind
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cola));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cola_alt));
                            this.rotateSegment(cell, false);
                            break;

                        case Consts.SNAKE_HEAD_CRASH:
                            // Collision death animation
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza_crash));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza_crash_alt));
                            this.rotateSegment(cell, true);
                            break;

                        case Consts.APPLE:
                            // Standard red apple collectible
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.apple_up : R.drawable.apple_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.apple_up_alt : R.drawable.apple_down_alt));
                            break;

                        case Consts.GREEN_APPLE:
                            // Green apple collectible
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.apple_verde_down : R.drawable.apple_verde_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.apple_verde_down_alt : R.drawable.apple_verde_down_alt));
                            break;

                        case Consts.GOLDEN_APPLE:
                            // High-value golden apple collectible
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.apple_dorada_down : R.drawable.apple_dorada_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.apple_dorada_down_alt : R.drawable.apple_dorada_down_alt));
                            break;

                        case Consts.BOMB:
                            // Bomb obstacle
                            if (isEvenCell)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.bomb_down : R.drawable.bomb_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up) ?
                                        R.drawable.bomb_down_alt : R.drawable.bomb_down_alt));
                            break;
                    }

                    // Apply calculated cell dimensions (sized in Control.java)
                    cell.getLayoutParams().width   = cellSize;
                    cell.getLayoutParams().height  = cellSize;
                }
            }
            Board.rotation += 180;
            gridLayout.invalidate();
        }

        // Operation 2: Display overlay messages (bomb warning countdown or game over)
        else if (((int)((Object[])arg)[0]) == Consts.EXECUTE_UPDATE_MESSAGE) {
            Object msgArg = ((Object[])arg)[1];

            // Show countdown before bombs appear on the board
            if (msgArg instanceof Integer && (Integer) msgArg == Consts.MSG_SPAWN_BOMB) {
                final Timer countdown = new Timer();

                class Msg {
                    public int i;
                    public Msg(int i) {
                        this.i = i;
                    }
                }
                final Msg seconds = new Msg(4);

                countdown.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        seconds.i--;
                        message.setText("BOMBS IN:  " + seconds.i + "...");
                        if (seconds.i == 0) {
                            countdown.cancel();
                            message.setText("");
                        }
                    }
                }, 1000, 1000);
            }
            // Trigger custom Game Over dialog on death
            else if (msgArg instanceof String && msgArg.equals("GAME OVER")) {
                message.setText("");
                ((com.snakegame.activities.MainActivity)container.getContext()).showGameOverDialog();
            }
        }

        // Operation 3: Toggle animation phase for bouncing food/items
        else if (((int)((Object[])arg)[0]) == Consts.EXECUTE_UP) {
            this.up = !this.up;
        }
    }

    /**
     * Rotates body segments and handles corner turns so snake pieces connect seamlessly.
     */
    private void rotateSegment(int i, int j, ImageView cell) {
        for (int k = 0; k < rotatableCells.size(); k++) {
            if (rotatableCells.get(k)[0] == i && rotatableCells.get(k)[1] == j) {

                // Corner turn detected: adjacent segments have different directions
                if (k > 0 && rotatableCells.get(k)[2] != rotatableCells.get(k - 1)[2]) {
                    boolean isEvenCell = (i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0);
                    if (isEvenCell)
                        cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_curva));
                    else
                        cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_curva_alt));

                    // Rotate corner piece based on entry and exit directions
                    if (rotatableCells.get(k)[2] == Consts.direction.UP.toInt()) {
                        if (rotatableCells.get(k - 1)[2] == Consts.direction.LEFT.toInt()) cell.setRotation(180);
                        else cell.setRotationX(180);
                    }
                    if (rotatableCells.get(k)[2] == Consts.direction.DOWN.toInt()) {
                        if (rotatableCells.get(k - 1)[2] == Consts.direction.RIGHT.toInt()) cell.setRotation(0);
                        else cell.setRotationY(180);
                    }
                    if (rotatableCells.get(k)[2] == Consts.direction.RIGHT.toInt()) {
                        if (rotatableCells.get(k - 1)[2] == Consts.direction.UP.toInt()) cell.setRotationY(180);
                        else {
                            cell.setRotation(270);
                            cell.setRotationX(180);
                        }
                    }
                    if (rotatableCells.get(k)[2] == Consts.direction.LEFT.toInt()) {
                        if (rotatableCells.get(k - 1)[2] != Consts.direction.DOWN.toInt()) cell.setRotation(90);
                        cell.setRotationX(180);
                    }
                }
                // Straight segment: align with current movement direction
                else {
                    cell.setRotation(rotation);

                    if (rotatableCells.get(k)[2] == Consts.direction.UP.toInt())
                        cell.setRotation(Board.rotation + 90);
                    if (rotatableCells.get(k)[2] == Consts.direction.DOWN.toInt())
                        cell.setRotation(Board.rotation + 270);
                    if (rotatableCells.get(k)[2] == Consts.direction.RIGHT.toInt())
                        cell.setRotation(Board.rotation);
                    if (rotatableCells.get(k)[2] == Consts.direction.LEFT.toInt())
                        cell.setRotation(Board.rotation);
                }
            }
        }
    }

    /**
     * Rotates head and tail to match the heading or collision direction.
     * @param isHead true if rotating the head; false if rotating the tail
     */
    private void rotateSegment(ImageView cell, boolean isHead) {
        if (isHead) {
            Consts.direction targetDir = (collisionDirection == null) ? headDirection : collisionDirection;
            if (targetDir == Consts.direction.UP) cell.setRotation(90);
            if (targetDir == Consts.direction.DOWN) cell.setRotation(270);
            if (targetDir == Consts.direction.LEFT) cell.setRotation(0);
            if (targetDir == Consts.direction.RIGHT) cell.setRotation(180);
        }
        else {
            if (tailDirection == Consts.direction.UP) {
                cell.setRotation(90);
                cell.setRotationY(Board.rotation);
            }
            if (tailDirection == Consts.direction.DOWN) {
                cell.setRotation(270);
                cell.setRotationY(Board.rotation);
            }
            if (tailDirection == Consts.direction.LEFT) {
                cell.setRotationX(Board.rotation);
            }
            if (tailDirection == Consts.direction.RIGHT) {
                cell.setRotation(180);
                cell.setRotationX(Board.rotation + 180);
            }
        }
    }
}
