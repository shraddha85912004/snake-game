package com.snakegame.constants;

/**
 * Global Constants for the Snake Game.
 * Contains board dimensions, timing parameters, command IDs for Observer events,
 * direction definitions, and numerical identifiers for grid objects.
 */
public class Consts
{
    // Board Grid Dimensions & Difficulty
    public static final int ROWS                    = 11;  // Number of rows on the game board
    public static final int COLS                    = 10;  // Number of columns on the game board
    public static final int NEXT_LEVEL              = 200; // Score required to spawn the golden apple and advance level
    public static final int BOMB_DIFFICULTY         = 3;   // Density factor determining how many bombs spawn

    // Timing, Speed & Acceleration Parameters
    public static final int SNAKE_TIME              = 350;  // Base movement tick delay in milliseconds (starting speed)
    public static final int SIZE_THRESHOLD          = 6;    // Snake body length threshold before speed scaling begins
    public static final double SPEED_DECREASE_FACTOR = 0.95; // 5% reduction in step delay per segment (accelerates speed)
    public static final int MIN_SNAKE_TIME          = 80;   // Maximum allowed speed cap (minimum delay in milliseconds)
    public static final int SECOND                  = 1000; // Milliseconds in one second (game clock interval)
    public static final int APPLE_TIME              = 1000; // Interval for apple spawn checks in milliseconds

    // Observer Operation Command IDs (Dispatched to Views)
    public static final int EXECUTE_UPDATE_BOARD    = 1001; // Command ID to advance snake and redraw all grid cells
    public static final int EXECUTE_UPDATE_SCORE    = 1002; // Command ID to refresh points and high score on HUD
    public static final int EXECUTE_UPDATE_TIME_TXT = 1003; // Command ID to update the elapsed time display (HH:MM:SS)
    public static final int EXECUTE_UPDATE_HINT     = 1004; // Command ID to show temporary hint text or alert icon
    public static final int EXECUTE_UPDATE_MESSAGE  = 1005; // Command ID to trigger bomb warning countdown or game over
    public static final int EXECUTE_TIMER           = 1006; // Command ID to increment game seconds, minutes, and hours
    public static final int EXECUTE_SPAWN_APPLE     = 1007; // Command ID to spawn a new apple on an empty cell
    public static final int EXECUTE_SPAWN_BOMB      = 1008; // Command ID to spawn temporary bomb obstacles
    public static final int EXECUTE_UP              = 1009; // Command ID to toggle the item bounce animation phase

    // Movement Direction Enumeration
    public static enum direction {
        UP(1), DOWN(-1), RIGHT(2), LEFT(-2);

        private int order;
        private direction(int order) { this.order = order; }
        public int toInt() { return this.order; }

        public static Consts.direction toDirection(int order) {
            if (order == 1) return direction.UP;
            else if (order == -1) return direction.DOWN;
            else if (order == 2) return direction.RIGHT;
            else if (order == -2) return direction.LEFT;
            else return null;
        }
    }

    // Initial Snake State
    public static final int INITIAL_POSITION_I               = Consts.ROWS / 2; // Starting row (middle of board)
    public static final int INITIAL_POSITION_J               = Consts.COLS - 4; // Starting column
    public static final int INITIAL_LENGTH                   = 4;              // Starting length of snake
    public static final Consts.direction INITIAL_ORIENTATION = direction.LEFT; // Starting movement direction

    // In-Game Hint Messages
    public static final String GOOD_BOMB            = "Bomb: No points deducted :)";
    public static final String BAD_BOMB             = "Bomb: -1 point";
    public static final String GAME_OVER            = "Game Over ¯\\_(ツ)_/¯";

    // Broadcast Message Subtypes
    public static final int MSG_SPAWN_BOMB          = 2001; // Subtype signaling the 4-second bomb alert countdown

    // Grid Cell Object Identifiers (Values stored in Game.grid[i][j])
    public static final int EMPTY1                  = 10; // Light green grass checkered tile
    public static final int EMPTY2                  = 20; // Dark green grass checkered tile
    public static final int SNAKE                   = 30; // Snake body segment
    public static final int SNAKE_HEAD              = 31; // Snake head
    public static final int SNAKE_HEAD_CRASH        = 32; // Snake head crash / collision graphic
    public static final int SNAKE_TAIL              = 33; // Snake tail segment
    public static final int APPLE                   = 40; // Standard red apple (+1 point)
    public static final int GREEN_APPLE             = 41; // Green bonus apple (+10 points)
    public static final int GOLDEN_APPLE            = 42; // Golden apple (level completion)
    public static final int BOMB                    = 43; // Bomb hazard
    public static final int POISON                  = 44; // Poison obstacle
    public static final int SAFE_AREA               = 45; // Safe spawn area
    public static final int RACCOON                 = 50; // Boss 1 identifier
    public static final int FOX                     = 51; // Boss 2 identifier
    public static final int COYOTE                  = 52; // Boss 3 identifier
    public static final int MONGOOSE                = 53; // Final boss identifier
}
