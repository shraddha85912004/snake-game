package com.andysierra.snakegame.views;

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
import com.andysierra.snakegame.R;
import com.andysierra.snakegame.activities.MainActivity;
import com.andysierra.snakegame.constants.Consts;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class Board <T extends ViewGroup> implements Observer
{
    private static final String TAG="Board";
    private static int rotacion = 0;
    public GridLayout gridLayout;
    public LinearLayout layoutMensajes;
    public TextView message;
    public Button btnRestart, btnExit;
    public static String hint;
    private int[][] grid;
    private ArrayList<int[]> rotatableCells;
    private Consts.direction headDirection, direccionColision, tailDirection;
    private boolean up;
    public T container;
    private MainActivity mainActivity;
    public int cellSize;


    // CONSTRUCTOR: Inicializar los objetos
    public Board(T container, MainActivity mainActivity) {
        this.up             = true;
        this.container     = container;
        this.mainActivity   = mainActivity;
        gridLayout          = new GridLayout(this.mainActivity);
        layoutMensajes      = new LinearLayout(mainActivity);
        message             = new TextView(mainActivity);
        btnRestart        = new Button(mainActivity);
        btnExit            = new Button(mainActivity);
        grid              = null;
        rotatableCells      = null;
        headDirection     = null;
        this.preparar();
    }




    // PREPARA LOS ELEMENTOS DEL TABLERO
    private void preparar() {
        gridLayout.setColumnCount(Consts.COLS);
        gridLayout.setRowCount(Consts.ROWS);
        layoutMensajes.setBackgroundColor(Color.TRANSPARENT);
        layoutMensajes.setGravity(Gravity.CENTER);
        layoutMensajes.setOrientation(LinearLayout.VERTICAL);
        message.setText("");
        message.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        message.setTextSize(40);
        message.setGravity(Gravity.CENTER);
        message.setTypeface(null, Typeface.BOLD);
        message.setTextColor(Color.WHITE);
        btnRestart.setText("Jugar de nuevo");
        btnRestart.setVisibility(View.INVISIBLE);
        btnRestart.setTag("btnRestart");
        btnExit.setTag("btnExit");
        btnExit.setVisibility(View.INVISIBLE);
        btnExit.setText("Salir");

        container.addView(gridLayout);
        container.addView(layoutMensajes);
        layoutMensajes.addView(message);
        layoutMensajes.addView(btnRestart);
        layoutMensajes.addView(btnExit);
        layoutMensajes.getLayoutParams().height = 1000;
    }




    // Control tendrá los listeners
    public void setOnClickListener(View.OnClickListener control) {
        this.btnRestart.setOnClickListener(control);
        this.btnExit.setOnClickListener(control);
    }





    // UPDATE: Dibuja el board de acuerdo a la grid en Logica
    @Override
    public void update(Observable o, Object arg) {

        if(((int)((Object[])arg)[0]) == Consts.EXECUTE_UPDATE_BOARD) {
            // Remover celdas viejas
            if(gridLayout.getChildCount()>0) gridLayout.removeAllViews();


            this.grid             = ((int[][])((Object[])arg)[1]);
            this.rotatableCells     = ((ArrayList<int[]>)((Object[])arg)[2]);
            this.headDirection    = ((Consts.direction)((Object[])arg)[3]);
            this.tailDirection      = ((Consts.direction)((Object[])arg)[4]);
            this.direccionColision  = ((Consts.direction)((Object[])arg)[5]);

            ImageView cell;

            // Para cada cell:
            for(int i=0; i<Consts.ROWS; i++) {
                for(int j=0; j<Consts.COLS; j++) {

                    // Poner una Imageview en el gridLayout
                    cell = new ImageView(mainActivity);
                    gridLayout.addView(cell);

                    // Dibujar cell
                    boolean b=(i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0);
                    switch (grid[i][j]) {
                        case Consts.EMPTY1:
                            cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_greenie));
                            break;

                        case Consts.EMPTY2:
                            cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_greenie_alt));
                            break;

                        case Consts.SNAKE:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_alt));
                            this.rotateSegment(i, j, cell);
                            break;

                        case Consts.SNAKE_HEAD:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza_alt));
                            this.rotateSegment(cell, true);
                            break;

                        case Consts.SNAKE_TAIL:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cola));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cola_alt));
                            this.rotateSegment(cell, false);
                            break;

                        case Consts.SNAKE_HEAD_CRASH:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza_crash));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_cabeza_crash_alt));
                            this.rotateSegment(cell, true);
                            break;

                        case Consts.APPLE:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.apple_up : R.drawable.apple_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.apple_up_alt : R.drawable.apple_down_alt));
                            break;

                        case Consts.GREEN_APPLE:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.apple_verde_down : R.drawable.apple_verde_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.apple_verde_down_alt : R.drawable.apple_verde_down_alt));
                            break;

                        case Consts.GOLDEN_APPLE:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.apple_dorada_down : R.drawable.apple_dorada_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.apple_dorada_down_alt : R.drawable.apple_dorada_down_alt));
                            break;

                        case Consts.BOMB:
                            if(b)
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.bomb_down : R.drawable.bomb_down));
                            else
                                cell.setImageDrawable(mainActivity.getDrawable((this.up)?
                                        R.drawable.bomb_down_alt : R.drawable.bomb_down_alt));
                            break;
                    }

                    // Tamaño de cell (Obtenido desde Control.java)
                    cell.getLayoutParams().width   = cellSize;
                    cell.getLayoutParams().height  = cellSize;
                }
            }
            Board.rotacion += 180;
            gridLayout.invalidate();
        }

        else if(((int)((Object[])arg)[0]) == Consts.EXECUTE_UPDATE_MESSAGE) {
            Object msgArg = ((Object[])arg)[1];
            if (msgArg instanceof Integer && (Integer) msgArg == Consts.MSG_SPAWN_BOMB) {
                final Timer countdown = new Timer();

                class Msg{
                    public int i;
                    public Msg(int i) {
                        this.i = i;
                    }
                }
                final Msg seconds = new Msg(4);

                countdown.schedule(new TimerTask()
                {
                    @Override
                    public void run() {
                        seconds.i--;
                        message.setText("bombs en "+seconds.i+"...");
                        if(seconds.i==0) {
                            countdown.cancel();
                            message.setText("");
                        }
                    }
                }, 1000,1000);
            } else if (msgArg instanceof String && msgArg.equals("GAME OVER")) {
                message.setText("");
                ((com.andysierra.snakegame.activities.MainActivity)container.getContext()).showGameOverDialog();
            }
        }

        else if(((int)((Object[])arg)[0]) == Consts.EXECUTE_UP) this.up = !this.up;
    }



    private void rotateSegment(int i, int j, ImageView cell) {
        for(int k=0; k<rotatableCells.size(); k++) {
            if(rotatableCells.get(k)[0]==i && rotatableCells.get(k)[1]==j){


                // ROTACIÓN DE LA CURVA
                if(k>0 && rotatableCells.get(k)[2] != rotatableCells.get(k-1)[2]) {
                    if((i%2==0 && j%2==0) || (i%2!=0 && j%2!=0))
                        cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_curva));
                    else
                        cell.setImageDrawable(mainActivity.getDrawable(R.drawable.ic_snake_curva_alt));

                    // Rotar los segmentos intermedios de la snake
                    if(rotatableCells.get(k)[2]==Consts.direction.UP.toInt()) {
                        if(rotatableCells.get(k-1)[2]==Consts.direction.LEFT.toInt()) cell.setRotation(180);
                        else cell.setRotationX(180);
                    }
                    if(rotatableCells.get(k)[2]==Consts.direction.DOWN.toInt()) {
                        if(rotatableCells.get(k-1)[2]==Consts.direction.RIGHT.toInt()) cell.setRotation(0);
                        else cell.setRotationY(180);
                    }
                    if(rotatableCells.get(k)[2]==Consts.direction.RIGHT.toInt()) {
                        if(rotatableCells.get(k-1)[2]==Consts.direction.UP.toInt()) cell.setRotationY(180);
                        else {
                            cell.setRotation(270);
                            cell.setRotationX(180);
                        }
                    }
                    if(rotatableCells.get(k)[2]==Consts.direction.LEFT.toInt()) {
                        if (rotatableCells.get(k-1)[2]!=Consts.direction.DOWN.toInt()) cell.setRotation(90);
                        cell.setRotationX(180);
                    }
                }

                else {
                    cell.setRotation(rotacion);

                    // Rotar los segmentos intermedios de la snake
                    if(rotatableCells.get(k)[2]==Consts.direction.UP.toInt())
                        cell.setRotation(Board.rotacion+90);
                    if(rotatableCells.get(k)[2]==Consts.direction.DOWN.toInt())
                        cell.setRotation(Board.rotacion+270);
                    if(rotatableCells.get(k)[2]==Consts.direction.RIGHT.toInt())
                        cell.setRotation(Board.rotacion);
                    if(rotatableCells.get(k)[2]==Consts.direction.LEFT.toInt())
                        cell.setRotation(Board.rotacion);
                }
            }
        }
    }
    // Sobrecarga
    private void rotateSegment(ImageView cell, boolean cabeza) {
        if(cabeza) {
            Consts.direction aux = (direccionColision==null)? headDirection : direccionColision;
            if(aux == Consts.direction.UP) cell.setRotation(90);
            if(aux == Consts.direction.DOWN) cell.setRotation(270);
            if(aux == Consts.direction.LEFT) cell.setRotation(0);
            if(aux == Consts.direction.RIGHT) cell.setRotation(180);
        }
        else {
            if(tailDirection == Consts.direction.UP) {
                cell.setRotation(90);
                cell.setRotationY(Board.rotacion);
            }
            if(tailDirection == Consts.direction.DOWN) {
                cell.setRotation(270);
                cell.setRotationY(Board.rotacion );
            }
            if(tailDirection == Consts.direction.LEFT) cell.setRotationX(Board.rotacion);
            if(tailDirection == Consts.direction.RIGHT) {
                cell.setRotation(180);
                cell.setRotationX(Board.rotacion+180);
            }
        }
    }
}
