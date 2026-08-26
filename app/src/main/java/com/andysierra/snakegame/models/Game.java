package com.andysierra.snakegame.models;

import android.util.Log;
import com.andysierra.snakegame.activities.MainActivity;
import com.andysierra.snakegame.constants.Consts;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import com.andysierra.snakegame.data.HighScoreManager;

public class Game extends Observable
{
    private static final String TAG="Game";
    private ArrayList<Observer> observers;
    WeakReference<MainActivity> weakActivity;
    private HighScoreManager highScoreManager;
    public static int[][] grid;
    private int randomSecond;
    public static int   score,
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
    long snakeMovement;    // Velocidad de snake
    long time;                 // Segundero


    // CONSTRUCTOR: Inicializar los objetos
    public Game(WeakReference<MainActivity> weakActivity, Snake... snakes) {
        this.weakActivity = weakActivity;   // Referencia débil de MainActivity
        if (weakActivity.get() != null) {
            this.highScoreManager = new HighScoreManager(weakActivity.get());
        }
        try {
            this.snakes = snakes;
            if(snakes.length<1)
                throw new InstantiationException("Al instanciar un objeto game, debes pasar como " +
                        "parámetro al menos una snake");
        }
        catch (InstantiationException i) {
            Log.e(TAG, i.getMessage(), i);
            ((MainActivity)weakActivity.get()).exitGame();
        }
        observers = new ArrayList<>();   // Mis observers
        this.isPaused = true;
    }

    public HighScoreManager getHighScoreManager() {
        return this.highScoreManager;
    }




    // CONSTRUCTOR: Inicializar los objetos
    public void prepareGame() {
        Game.grid = new int[Consts.ROWS][Consts.COLS];

        // iniciar backgrund ajedrezao

        for(int i=0; i<Consts.ROWS; i++)
            for(int j=0; j<Consts.COLS; j++)
                Game.grid[i][j] = ((i%2==0 && j%2==0) || (i%2!=0 && j%2!=0))?
                        Consts.EMPTY1 : Consts.EMPTY2;

        // reiniciar valores del game
        Game.score = 0;
        Game.seconds = 0;
        Game.minutes = 0;
        Game.hours = 0;
        Game.applesInGame = 0;
        Game.bombsInGame = 0;
        Game.appleCounter = 0;
        Game.poisonEffect = 1;
        randomSecond = ((int)(Math.random()*50+10));

        // reiniciar dirección
        this.direction = Consts.direction.LEFT;

        // reiniciar snake
        snakes[0].resetSnake();

        // Colocar snake
        Game.grid[snakes[0].headI][snakes[0].headJ] = Consts.SNAKE_HEAD;
        snakes[0].addSegments();

        // execute game
        if( ((MainActivity)weakActivity.get()) != null &&
            !((MainActivity)weakActivity.get()).isFinishing() ) { this.looper(); }
    }





    // Obtener una cell vacía para poner apples
    private int[] getEmptyCell() {
        int i = (int)(Math.random()*Consts.ROWS);
        int j = (int)(Math.random()*Consts.COLS);

        while(Game.grid[i][j] != Consts.EMPTY1 && Game.grid[i][j] != Consts.EMPTY2) {
            i = (int)(Math.random()*Consts.ROWS);
            j = (int)(Math.random()*Consts.COLS);
        }
        return new int[]{i, j};
    }





    // Loop principal del game
    private void looper() {
        isActive = true;
        isPaused  = true;
        if(loop == null) {
            loop = new Thread(new Runnable()    // Hilo del loop
            {
                @Override
                public void run() {
                    snakeMovement = System.currentTimeMillis();    // Iniciar fps
                    time              = System.currentTimeMillis();    // Iniciar el time

                    while (isActive) {                      // Mientras el game esté activo


                        // Ejecute a una velocidad time  (Movimiento snake)
                        // SI ESTÁ EN PAUSA, LA SNAKE SE ANIMA PERO NO SE MUEVE DE SU POSICIÓN
                        if(System.currentTimeMillis()- snakeMovement > Consts.SNAKE_TIME) {
                            execute(Consts.EXECUTE_UP);                        // Tic/Tac de animación
                            execute(Consts.EXECUTE_UPDATE_BOARD);        // refrescar pantalla
                            execute(Consts.EXECUTE_UPDATE_SCORE);        // Actualizar score
                            snakeMovement= System.currentTimeMillis();
                        }


                        if(!isPaused) {                      // Si el game no está en Pausa

                            // Obtenga el time transcurrido (requerimiento)
                            if(System.currentTimeMillis()- time > Consts.SECOND) {
                                if(!isPaused)
                                    execute(Consts.EXECUTE_TIMER);          // Actualizar Tiempo
                                execute(Consts.EXECUTE_UPDATE_TIME_TXT);      // Actualizar reloj
                                execute(Consts.EXECUTE_UPDATE_MESSAGE);        // Actualizo mensajes
                                if(Game.applesInGame<1)
                                    execute(Consts.EXECUTE_SPAWN_APPLE);         // Spawnear apples
                                if(randomSecond == seconds)
                                    execute(Consts.EXECUTE_SPAWN_BOMB);           // Spawnear bombs
                                time= System.currentTimeMillis();
                            }
                        }
                    }
                }
            },"loop_del_juego");
            loop.start();   // Iniciar el loop
        }
    }




    private synchronized void execute(int operation) {

        // EJECUTAR TIC TAC DE ANIMACIÓN
        if(operation == Consts.EXECUTE_UP) {
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable()
            {
                @Override
                public void run() {
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UP
                    });
                }
            });
        }

        // EJECUTAR MOVIMIENTO DE SNAKE
        if(operation == Consts.EXECUTE_UPDATE_BOARD) {
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable()
            {
                @Override
                public void run() {             // Notificar en el Thread principal

                    // Si la cabeza no se mueve, muestre que se estrelló y pause el game,
                    // o sino siga actualizando el board.

                    if(!isPaused)
                        if(!snakes[0].move(direction)) {
                            Game.grid[snakes[0].headI][snakes[0].headJ] =
                                    Consts.SNAKE_HEAD_CRASH;
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

                    if(snakes[0].nextLevel) {
                        prepareGame();
                    }

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

        else if(operation == Consts.EXECUTE_UPDATE_SCORE) {
            if (highScoreManager != null) {
                highScoreManager.saveHighScore(Game.score);
            }
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable()
            {
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

        // TEMPORIZADOR
        else if(operation == Consts.EXECUTE_TIMER) {
            seconds++;
            if(seconds>59) {
                minutes++;
                seconds=0;
                randomSecond = ((int)(Math.random()*60));
            }
            if(minutes>59) {
                hours++;
                minutes=0;
            }
            if(hours>24) hours=0;
        }

        else if(operation == Consts.EXECUTE_UPDATE_TIME_TXT) {
            ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable()
            {
                @Override
                public void run() {
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_TIME_TXT,
                            (hours>0)?
                                    ""+hours+":"+((minutes<10)?"0"+minutes:minutes)+":"+((seconds<10)?"0"+seconds:seconds):
                                    ((minutes<10)?"0"+minutes:minutes)+":"+((seconds<10)?"0"+seconds:seconds)
                    });
                }
            });
        }

        // SPAWN DE appleS
        else if(operation == Consts.EXECUTE_SPAWN_APPLE) {

            if(Game.score < Consts.NEXT_LEVEL) {
                // Si la snake está envenenada, no puede comer apples
                if(poisonEffect>1) poisonEffect--;

                // Aparecer apple roja o apple verde
                if(seconds%poisonEffect==0) {
                    int[] coords = this.getEmptyCell();
                    Game.grid[coords[0]][coords[1]] =
                            (appleCounter>0 && appleCounter%7==0) ?
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

        // SPAWN DE bombS
        else if(operation == Consts.EXECUTE_SPAWN_BOMB) {
            if(bombsInGame==0) {
                for(int i=0; i<(Consts.ROWS*Consts.COLS)/(10- Consts.BOMB_DIFFICULTY); i++) {
                    int[] coords = this.getEmptyCell();
                    Game.grid[coords[0]][coords[1]] = Consts.BOMB;
                    bombsInGame++;
                }
            }

            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run() {
                    bombsInGame = 0;
                    for(int i=0; i<Consts.ROWS; i++)
                        for(int j=0; j<Consts.COLS; j++)
                            if(grid[i][j] == Consts.BOMB)
                                grid[i][j] = ((i%2==0 && j%2==0) || (i%2!=0 && j%2!=0))?
                                        Consts.EMPTY1 : Consts.EMPTY2;
                }
            }, 5000);
        }

        // ACTUALIZAR MENSAJE
        else if(operation == Consts.EXECUTE_UPDATE_MESSAGE) {

            // Si estoy a punto de soltar bombs, muestre el message
            if((randomSecond-seconds)==4) {
                ((MainActivity)weakActivity.get()).runOnUiThread(new Runnable()
                {
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
    public void notifyObservers(Object arg) { for(Observer o : observers) o.update(this, arg); }
}
