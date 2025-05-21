// File: Display.java
package ch.carlopezzotti.engine;
// File: Display.java

import ch.carlopezzotti.engine.Engine.Color;

public interface  Display {
    public void draw(int[] pixels, int width2, int height2);
    public void draw(int[] pixels, int width, int height, Color[] cols);
    public void init();
    public default void clear() {
        System.out.print("\u001B[H\u001B[2J"); // clear
        System.out.print("\u001B[?25l");       // hide cursor
        System.out.flush();
    }
}
