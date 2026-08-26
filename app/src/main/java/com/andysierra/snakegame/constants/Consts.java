package com.andysierra.snakegame.constants;

public class Consts
{
    // Board y Lógica
    public static final int ROWS               = 11;
    public static final int COLS                = 10;
    public static final int NEXT_LEVEL     = 200; // default 200
    public static final int BOMB_DIFFICULTY   = 3;  // default 3

    // Medidas de time y FPS
    public static final int SNAKE_TIME            = 350;          // Velocidad de snake, defaults 150, 250, 350
    public static final int SECOND                     = 1000;         // segundero
    public static final int APPLE_TIME              = 1000;         // aparición de apples

    public static final int EXECUTE_UPDATE_BOARD      = 1001;         // id snake
    public static final int EXECUTE_UPDATE_SCORE      = 1002;         // id score
    public static final int EXECUTE_UPDATE_TIME_TXT    = 1003;         // id reloj
    public static final int EXECUTE_UPDATE_HINT         = 1004;         // id hint
    public static final int EXECUTE_UPDATE_MESSAGE      = 1005;         // id message
    public static final int EXECUTE_TIMER            = 1006;         // id temporizador
    public static final int EXECUTE_SPAWN_APPLE           = 1007;         // id spawn apples
    public static final int EXECUTE_SPAWN_BOMB             = 1008;         // id spawn bomb
    public static final int EXECUTE_UP                      = 1009;         // id up


    // Snake
    public static enum direction{
        UP(1), DOWN(-1), RIGHT(2), LEFT(-2);
        private int order;
        private direction(int order) { this.order = order; }
        public int toInt() { return this.order; }
        public static Consts.direction toDirection(int order) {
            if(order==1) return direction.UP;
            else if(order==-1) return direction.DOWN;
            else if(order==2) return direction.RIGHT;
            else if(order==-2) return direction.LEFT;
            else return null;
        }
    }
    public static final int INITIAL_POSITION_I                  = Consts.ROWS/2;
    public static final int INITIAL_POSITION_J                  = Consts.COLS-4;
    public static final int INITIAL_LENGTH                    = 4;
    public static final Consts.direction INITIAL_ORIENTATION    = direction.LEFT;

    // Hints
    public static final String GOOD_BOMB      = "bomb: No te quitará puntos por ahora :)";
    public static final String BAD_BOMB       = "bomb: -10 puntos";
    public static final String GAME_OVER        = "Game Over ¯\\_(ツ)_/¯";

    // Mensajes
    public static final int MSG_SPAWN_BOMB    = 2001;

    // Objetos del game
    public static final int EMPTY1                      = 10;
    public static final int EMPTY2                      = 20;
    public static final int SNAKE                   = 30;
    public static final int SNAKE_HEAD            = 31;
    public static final int SNAKE_HEAD_CRASH      = 32;
    public static final int SNAKE_TAIL              = 33;
    public static final int APPLE                     = 40;
    public static final int GREEN_APPLE               = 41;
    public static final int GOLDEN_APPLE              = 42;
    public static final int BOMB                       = 43;
    public static final int POISON                      = 44;
    public static final int SAFE_AREA                 = 45;
    public static final int RACCOON                     = 50;   // Primer   jefe
    public static final int FOX                       = 51;   // Segundo  Jefe
    public static final int COYOTE                      = 52;   // Tercer   Jefe
    public static final int MONGOOSE                    = 53;   // Último   Jefe
}
