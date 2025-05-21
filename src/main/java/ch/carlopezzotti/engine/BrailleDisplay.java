// File: src/ch/carlopezzotti/engine/BrailleDisplay.java
package ch.carlopezzotti.engine;

import java.io.IOException;

import ch.carlopezzotti.engine.Engine.Color;

public class BrailleDisplay implements Display {
    @Override
    public void init() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                // Windows: abilita UTF-8
                new ProcessBuilder("cmd", "/c", "chcp", "65001")
                        .inheritIO().start().waitFor();
                System.out.println("→ Usare Windows Terminal con font Segoe UI Emoji");
            } else {
                // Unix: metti terminale in raw mode (no echo, no canonical)
                new ProcessBuilder("sh", "-c", "stty raw -echo </dev/tty")
                        .inheritIO().start().waitFor();
                // Ripristino su exit
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        new ProcessBuilder("sh", "-c", "stty sane </dev/tty")
                                .inheritIO().start().waitFor();
                        // mostra cursore
                        System.out.print("\u001B[?25h");
                    } catch (Exception e) {
                        // ignore
                    }
                }));
            }
        } catch (Exception ignored) {
        }

        // Clear screen, move cursor home, hide cursor
        System.out.print("\u001B[2J"); // Clear entire screen
        System.out.print("\u001B[H"); // Cursor to home
        System.out.print("\u001B[?25l"); // Hide cursor
        System.out.flush();
    }

    @Override
    public void clear() {
        // Move cursor to home without clearing
        System.out.print("\u001B[H");
        System.out.flush();
    }

    @Override
    public void draw(int[] pixels, int w, int h, Color[] cols) {
        StringBuilder sb = new StringBuilder(w * h / 2);
        for (int by = 0; by < h; by += 4) {
            for (int bx = 0; bx < w; bx += 2) {
                int code = 0x2800;
                Color cellColor = null;
                for (int py = 0; py < 4; py++) {
                    int row = by + py;
                    if (row >= h)
                        break;
                    for (int px = 0; px < 2; px++) {
                        int col = bx + px;
                        if (col >= w)
                            continue;
                        int idx = row * w + col;
                        if (pixels[idx] != 0) {
                            int bit = py * 2 + px;
                            code |= 1 << bit;
                            if (cellColor == null)
                                cellColor = cols[idx];
                        }
                    }
                }
                if (cellColor != null)
                    sb.append(cellColor);
                sb.append((char) code);
                if (cellColor != null)
                    sb.append(Color.RESET);
            }
            sb.append('\n');
        }
        System.out.print(sb);
        System.out.flush();
    }

    @Override
    public void draw(int[] pixels, int width2, int height2) {
        draw(pixels, width2, height2, new Color[width2 * height2]);
    }
}