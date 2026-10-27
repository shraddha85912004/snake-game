package com.snakegame.models;

import android.util.Log;
import com.snakegame.constants.Consts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

/**
 * Snake Model (Observable)
 * Manages the snake's state: head and tail positions, body segments,
 * directional movement, growth, self-collision detection, and item consumption.
 */
public class Snake extends Observable
{
    private static final String TAG = "Snake";
    public Consts.direction headDirection;
    public Consts.direction tailDirection;
    public Consts.direction headCollisionDirection;
    public ArrayList<int[]> body;
    public ArrayList<Observer> observers;
    public boolean nextLevel;
    public int length;
    public int headI;
    public int headJ;
    public int tailI;
    public int tailJ;

    /**
     * Constructor: Initializes starting coordinates, initial length, and orientation.
     */
    public Snake(int headI, int headJ, int length, Consts.direction headDirection) {
        this.headI = headI;
        this.headJ = headJ;
        this.tailI = headI;
        this.tailJ = headJ;
        this.length = length;
        this.headDirection = headDirection;
        this.tailDirection = headDirection;
        this.headCollisionDirection = null;
        body = new ArrayList<>();
        observers = new ArrayList<>();
        nextLevel = false;
    }

    /**
     * Builds and attaches the initial body segments trailing behind the head.
     */
    public void addSegments() {
        for (int k = 1; k < this.length; k++) {
            if (body.size() == 0) {
                body.add(new int[]{headI, headJ + k, Consts.direction.LEFT.toInt()});
                tailJ++;
            }
            else {
                int lastDir = body.get(body.size() - 1)[2];
                if (lastDir == Consts.direction.UP.toInt()) {
                    if (tailI + 1 < Consts.ROWS) tailI++;
                    body.add(new int[]{tailI, tailJ, Consts.direction.UP.toInt()});
                }
                else if (lastDir == Consts.direction.DOWN.toInt()) {
                    if (tailI - 1 >= 0) tailI--;
                    body.add(new int[]{tailI, tailJ, Consts.direction.DOWN.toInt()});
                }
                else if (lastDir == Consts.direction.RIGHT.toInt()) {
                    if (tailJ - 1 >= 0) tailJ--;
                    body.add(new int[]{tailI, tailJ, Consts.direction.RIGHT.toInt()});
                }
                else if (lastDir == Consts.direction.LEFT.toInt()) {
                    if (tailJ + 1 < Consts.COLS) tailJ++;
                    body.add(new int[]{tailI, tailJ, Consts.direction.LEFT.toInt()});
                }
            }
        }

        // Reverse so body.get(0) represents the tail end of the snake
        Collections.reverse(body);

        // Mark intermediate body segments on the grid
        for (int i = 1; i < body.size(); i++) {
            Game.grid[body.get(i)[0]][body.get(i)[1]] = Consts.SNAKE;
        }

        // Orient and place the tail segment
        this.tailDirection = Consts.direction.toDirection(body.get(0)[2]);
        Game.grid[tailI][tailJ] = Consts.SNAKE_TAIL;
    }

    /**
     * Resets the snake back to its starting state and initial position.
     */
    public void resetSnake() {
        this.headI = Consts.INITIAL_POSITION_I;
        this.headJ = Consts.INITIAL_POSITION_J;
        this.tailI = headI;
        this.tailJ = headJ;
        this.length = Consts.INITIAL_LENGTH;
        this.headDirection = Consts.INITIAL_ORIENTATION;
        this.headCollisionDirection = null;
        this.tailDirection = this.headDirection;

        // Clear existing body segments from the grid
        for (int[] segment : this.body) {
            Game.grid[segment[0]][segment[1]] =
                    ((segment[0] % 2 == 0 && segment[1] % 2 == 0) || (segment[0] % 2 != 0 && segment[1] % 2 != 0)) ?
                            Consts.EMPTY1 : Consts.EMPTY2;
        }

        this.body.clear();
        this.body = new ArrayList<>();
        this.tailI = this.headI;
        this.tailJ = this.headJ;
        this.nextLevel = false;
    }

    /**
     * Moves the snake one step in the given direction.
     * Checks for wall and self-collisions, updates body coordinates, and handles growth.
     * @return true if the move was successful, false if a collision occurred.
     */
    public boolean move(Consts.direction direction) {
        // Prevent immediate 180-degree reversal into the snake's own neck
        if (direction.toInt() == (this.headDirection.toInt() * (-1))) {
            direction = Consts.direction.toDirection(this.headDirection.toInt());
        }

        this.headDirection = direction;
        boolean grow = true;

        // Save previous head coordinates before updating
        int[] previous = new int[]{headI, headJ, direction.toInt()};

        // Advance head position while verifying boundary walls and self-collisions
        if (direction == Consts.direction.UP && headI > 0) {
            if (!isCollision(direction)) headI--; else return false;
        } else if (direction == Consts.direction.DOWN && headI < Consts.ROWS - 1) {
            if (!isCollision(direction)) headI++; else return false;
        } else if (direction == Consts.direction.RIGHT && headJ < Consts.COLS - 1) {
            if (!isCollision(direction)) headJ++; else return false;
        } else if (direction == Consts.direction.LEFT && headJ > 0) {
            if (!isCollision(direction)) headJ--; else return false;
        } else {
            return false; // Hit outer boundary wall
        }

        // Handle item consumption at the new head position
        int growth = ateApple();
        if (growth > 0) {
            Game.score += growth;
        }
        else if (growth < 0) {
            // Shrink snake segments if hit by a hazard (down to minimum length)
            if (body.size() > 2) {
                for (int i = 0; i < (growth * (-1)); i++) {
                    Game.grid[body.get(0)[0]][body.get(0)[1]] =
                            ((body.get(0)[0] % 2 == 0 && body.get(0)[1] % 2 == 0) ||
                                    (body.get(0)[0] % 2 != 0 && body.get(0)[1] % 2 != 0)) ?
                                    Consts.EMPTY1 : Consts.EMPTY2;
                    body.remove(0);
                }
            }
            grow = false;
        }
        else if (growth == 0) {
            grow = false;
        }

        // Push previous head location onto the body list
        body.add(previous);

        // Render new head on grid
        Game.grid[headI][headJ] = Consts.SNAKE_HEAD;

        // Clear previous tail cell from grid
        Game.grid[body.get(0)[0]][body.get(0)[1]] =
                ((body.get(0)[0] % 2 == 0 && body.get(0)[1] % 2 == 0) ||
                 (body.get(0)[0] % 2 != 0 && body.get(0)[1] % 2 != 0)) ?
                        Consts.EMPTY1 : Consts.EMPTY2;

        // If the snake did not grow this tick, discard the old tail
        if (!grow) body.remove(0);

        // Update new tail position
        tailI = body.get(0)[0];
        tailJ = body.get(0)[1];

        // Mark remaining intermediate body segments
        for (int i = 1; i < body.size(); i++) {
            Game.grid[body.get(i)[0]][body.get(i)[1]] = Consts.SNAKE;
        }

        // Update and render the new tail segment
        this.tailDirection = Consts.direction.toDirection(body.get(0)[2]);
        Game.grid[tailI][tailJ] = Consts.SNAKE_TAIL;

        return true;
    }

    /**
     * Checks if moving in the given direction would collide with any existing body segment.
     */
    private boolean isCollision(Consts.direction direction) {
        for (int[] p : body) {
            if ((direction == Consts.direction.UP    && p[0] == headI - 1 && p[1] == headJ) ||
                (direction == Consts.direction.DOWN  && p[0] == headI + 1 && p[1] == headJ) ||
                (direction == Consts.direction.RIGHT && p[1] == headJ + 1 && p[0] == headI) ||
                (direction == Consts.direction.LEFT  && p[1] == headJ - 1 && p[0] == headI)) {
                this.headCollisionDirection = direction;
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates item consumption at the current head cell:
     * - Red Apple: +1 point, grows snake by 1
     * - Green Apple: +10 points, bonus collectible
     * - Golden Apple: advances level
     * - Bomb: penalty deduction (-10 points) or penalty protection depending on size
     * @return growth factor (>0 grow, <0 shrink, 0 no change)
     */
    private int ateApple() {
        if (Game.grid[this.headI][this.headJ] == Consts.APPLE) {
            Game.applesInGame--;
            Game.appleCounter++;
            return 1;
        }

        else if (Game.grid[this.headI][this.headJ] == Consts.GREEN_APPLE) {
            Game.applesInGame--;
            Game.appleCounter++;
            return 10;
        }

        else if (Game.grid[this.headI][this.headJ] == Consts.GOLDEN_APPLE) {
            this.nextLevel = true;
        }

        else if (Game.grid[this.headI][this.headJ] == Consts.BOMB) {
            if (body.size() < (Consts.ROWS * Consts.COLS) / 2) {
                // Deduct 10 points as penalty if player has enough score
                if (Game.score > 10) {
                    Game.score -= 10;
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_HINT,
                            Consts.BAD_BOMB
                    });
                }
                else {
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_HINT,
                            Consts.GOOD_BOMB
                    });
                }
            }
            else {
                // If snake is large enough, spare penalty
                notifyObservers(new Object[]{
                    Consts.EXECUTE_UPDATE_HINT,
                    Consts.GOOD_BOMB
                });
            }
            return -2;
        }
        else if (Game.grid[this.headI][this.headJ] == Consts.POISON) {
            Game.poisonEffect = 23; // Duration delay for poison effect
        }
        return 0;
    }

    @Override
    public synchronized void addObserver(Observer o) { observers.add(o); }

    @Override
    public void notifyObservers(Object arg) { for (Observer o : observers) o.update(this, arg); }
}
