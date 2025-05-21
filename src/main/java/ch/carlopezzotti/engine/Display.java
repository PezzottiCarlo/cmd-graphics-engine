package ch.carlopezzotti.engine;


import ch.carlopezzotti.engine.Engine.Color;

public interface  Display {
    public void draw(int[] pixels, int width2, int height2);
    public void draw(int[] pixels, int width, int height, Color[] cols);
    public void init();
    public default void clear() {
        System.out.print("\u001B[H\u001B[2J"); 
        System.out.print("\u001B[?25l");
        System.out.flush();
    }
}
