package com.andysierra.snakegame.models;

import android.util.Log;

import com.andysierra.snakegame.constants.Consts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

public class Snake extends Observable
{
    private static final String TAG="Snake";
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


    // CONSTRUCTOR: Inicializar los objetos
    public Snake(int headI, int headJ, int length, Consts.direction headDirection) {
        this.headI = headI;
        this.headJ = headJ;
        this.tailI = headI;
        this.tailJ = headJ;
        this.length = length;
        //Game.grid[headI][headJ] = Consts.SNAKE_HEAD;
        this.headDirection = headDirection;
        this.tailDirection = headDirection;
        this.headCollisionDirection = null;
        body= new ArrayList<>();
        observers= new ArrayList<>();
        nextLevel = false;
    }


    public void addSegments() {
        // Según la dirección, agrega un segment
        for(int k=1; k<this.length; k++) {
            if(body.size()==0) {
                body.add(new int[]{headI, headJ+k, Consts.direction.LEFT.toInt()});
                tailJ++;
            }
            else {
                if(body.get(body.size()-1)[2] == Consts.direction.UP.toInt()) {
                    if(tailI+1 < Consts.ROWS) tailI++;     // QUE PASA SI NO SE PUEDE AGREGAR?
                    body.add(new int[]{tailI, tailJ, Consts.direction.UP.toInt()});
                }
                else if(body.get(body.size()-1)[2] == Consts.direction.DOWN.toInt()) {
                    if(tailI-1 >= 0) tailI--;               // QUE PASA SI NO SE PUEDE AGREGAR?
                    body.add(new int[]{tailI, tailJ, Consts.direction.DOWN.toInt()});
                }
                else if(body.get(body.size()-1)[2] == Consts.direction.RIGHT.toInt()) {
                    if(tailJ-1 >= 0) tailJ--;               // QUE PASA SI NO SE PUEDE AGREGAR?
                    body.add(new int[]{tailI, tailJ, Consts.direction.RIGHT.toInt()});
                }
                else if(body.get(body.size()-1)[2] == Consts.direction.LEFT.toInt()) {
                    if(tailJ+1 < Consts.COLS) tailJ++;     // QUE PASA SI NO SE PUEDE AGREGAR?
                    body.add(new int[]{tailI, tailJ, Consts.direction.LEFT.toInt()});
                }
            }
        }

        // Acomodar segmentos (Ahora body.get(0) es la cola de la snake)
        Collections.reverse(body);

        // Pintar los demás segmentos
        for(int i=1; i<body.size(); i++)
            Game.grid[body.get(i)[0]][body.get(i)[1]] = Consts.SNAKE;

        this.tailDirection =
                Consts.direction.toDirection(body.get(0)[2]);              // Dirección de la cola
        Game.grid[tailI][tailJ] = Consts.SNAKE_TAIL;                        // Pintar la cola
    }


    public void resetSnake() {
        this.headI = Consts.INITIAL_POSITION_I;
        this.headJ = Consts.INITIAL_POSITION_J;
        this.tailI = headI;
        this.tailJ = headJ;
        this.length = Consts.INITIAL_LENGTH;
        this.headDirection = Consts.INITIAL_ORIENTATION;
        this.headCollisionDirection = null;
        this.tailDirection = this.headDirection;
        for(int[] segment : this.body)
            Game.grid[segment[0]][segment[1]] =
                    ((segment[0]%2==0 && segment[1]%2==0) || (segment[0]%2!=0 && segment[1]%2!=0))?
                            Consts.EMPTY1 : Consts.EMPTY2;
        this.body.clear();
        this.body = new ArrayList<>();
        this.tailI = this.headI;
        this.tailJ = this.headJ;
        this.nextLevel = false;
    }



    public boolean move(Consts.direction direction) {
        // No permitir reversa de snake
        if(direction.toInt() == (this.headDirection.toInt()*(-1)))
            direction = Consts.direction.toDirection(this.headDirection.toInt());

        // Dirección de la cabeza
        this.headDirection= direction;

        // Crecer?
        boolean grow = true;

        // Actualizar la nueva posición y evitar una colisión
        int[] previous = new int[]{headI, headJ,direction.toInt()};
        if(       direction == Consts.direction.UP    && headI > 0)
            if(!isCollision(direction)) headI--; else return false;
        else if ( direction == Consts.direction.DOWN     && headI < Consts.ROWS-1)
            if(!isCollision(direction)) headI++; else return false;
        else if ( direction == Consts.direction.RIGHT   && headJ < Consts.COLS-1)
            if(!isCollision(direction)) headJ++; else return false;
        else if ( direction == Consts.direction.LEFT && headJ > 0)
            if(!isCollision(direction)) headJ--; else return false;
        else return false;  // No se puede move

// SI EL MOVIMIENTO ES VÁLIDO:

        // Si come un objeto, altere el score y el tamaño de la snake
        int growth = ateApple();
        if(growth > 0) Game.score += growth;
        else if(growth < 0) {
            // Moche a la snake mientras su length esté entre 3 y la mitad del board
            if(body.size()>2) {
                for(int i=0; i<(growth*(-1)); i++) {

                    // Limpiar
                    Game.grid[body.get(0)[0]][body.get(0)[1]]=
                            ((body.get(0)[0] %2==0&& body.get(0)[1] %2==0) ||
                                    (body.get(0)[0] %2!=0&& body.get(0)[1] %2!=0))?
                                    Consts.EMPTY1 : Consts.EMPTY2;

                    // Mochar
                    body.remove(0);
                }
            }
            grow = false;
        }
        else if(growth == 0) grow = false;


        // Agregar la previous posición al body
        body.add(previous);

        // Pintar cabeza
        Game.grid[headI][headJ]  = Consts.SNAKE_HEAD;         // Pintar la nueva cabeza

        // Limpiar
        Game.grid[body.get(0)[0]][body.get(0)[1]]=
                ((body.get(0)[0] %2==0&& body.get(0)[1] %2==0) ||
                 (body.get(0)[0] %2!=0&& body.get(0)[1] %2!=0))?
                        Consts.EMPTY1 : Consts.EMPTY2;

        // Elimina la previous posición de la snake
        if(!grow) body.remove(0);

        // Nueva cola
        tailI = body.get(0)[0];
        tailJ = body.get(0)[1];

        // Pintar los demás segmentos
        for(int i=1; i<body.size(); i++)
            Game.grid[body.get(i)[0]][body.get(i)[1]] = Consts.SNAKE;

        // Pintar cola
        this.tailDirection =
               Consts.direction.toDirection(body.get(0)[2]);               // Dirección de la cola
        Game.grid[tailI][tailJ] = Consts.SNAKE_TAIL;                        // Pintar la cola

        return true;
    }



    // Detecta si la snake se colisionará consigo misma
    private boolean isCollision(Consts.direction direction) {
        for(int[] p : body)
            if( (direction == Consts.direction.UP    && p[0] == headI-1 && p[1] == headJ)    ||
                (direction == Consts.direction.DOWN     && p[0] == headI+1 && p[1] == headJ)    ||
                (direction == Consts.direction.RIGHT   && p[1] == headJ+1 && p[0] == headI)    ||
                (direction == Consts.direction.LEFT && p[1] == headJ-1 && p[0] == headI)) {
                this.headCollisionDirection = direction;
                return true;
            }
        return false;
    }




    // APARECER
    //      apple roja    +1 pto
    //      apple verde   +10 ptos
    //      apple dorada  (siguiente nivel)
    //      veneno          23 seconds sin apples
    //      bomb           -10 ptos
    private int ateApple() {
        if(Game.grid[this.headI][this.headJ] == Consts.APPLE) {
            Game.applesInGame--;
            Game.appleCounter++;
            return 1;
        }

        else if(Game.grid[this.headI][this.headJ] == Consts.GREEN_APPLE) {
            Game.applesInGame--;
            Game.appleCounter++;
            return 10;
        }

        else if(Game.grid[this.headI][this.headJ] == Consts.GOLDEN_APPLE) {
            this.nextLevel = true;
        }

        else if(Game.grid[this.headI][this.headJ] == Consts.BOMB) {

            if(body.size()<(Consts.ROWS*Consts.COLS)/2) {
                // Si tiene score, disminuya como castigo
                if(Game.score > 10) {
                    Game.score-=10;
                    notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_HINT,
                            Consts.BAD_BOMB
                    });
                }
                else notifyObservers(new Object[]{
                            Consts.EXECUTE_UPDATE_HINT,
                            Consts.GOOD_BOMB
                     });
            }
            else if(body.size()>(Consts.ROWS*Consts.COLS)/2) {
                // Si es muy largo o su score es muy pequeño, no castigará
                notifyObservers(new Object[]{
                    Consts.EXECUTE_UPDATE_HINT,
                    Consts.GOOD_BOMB
                });
            }


            return -2;
        }
        else if(Game.grid[this.headI][this.headJ] == Consts.POISON) {
            Game.poisonEffect = 23;  // Número primo para demora en envenenamiento
        }
        return 0;
    }

    @Override
    public synchronized void addObserver(Observer o) { observers.add(o); }

    @Override
    public void notifyObservers(Object arg) { for(Observer o : observers) o.update(this, arg); }
}
