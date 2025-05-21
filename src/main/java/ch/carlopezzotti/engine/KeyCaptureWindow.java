// File: src/ch/carlopezzotti/engine/KeyCaptureWindow.java
package ch.carlopezzotti.engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class KeyCaptureWindow extends JFrame {

    public interface KeyListener {
        void onKeyDown(KeyEvent e);
        void onKeyUp(KeyEvent e);
    }

    private final List<KeyListener> listeners = new CopyOnWriteArrayList<>();
    private Point mouseDragOffset;

    public KeyCaptureWindow() {
        super("KeyCapture");         // titolo per aiuto al focus
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(false);       // mantiene la barra del titolo per poter spostare
        setSize(100, 100);           // abbastanza grande da poter ricevere focus
        setAlwaysOnTop(true);
        setFocusable(true);

        // posiziona in basso a sinistra (sopra la taskbar)
        Rectangle usable = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getMaximumWindowBounds();
        setLocation(usable.x, usable.y + usable.height - getHeight());

        // Key events
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                listeners.forEach(l -> l.onKeyDown(e));
            }
            @Override
            public void keyReleased(KeyEvent e) {
                listeners.forEach(l -> l.onKeyUp(e));
            }
        });

        // Mouse events per drag-and-move della finestra
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseDragOffset = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - mouseDragOffset.x,
                            loc.y + e.getY() - mouseDragOffset.y);
            }
        });

        // Se perde il focus, riportala avanti
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    toFront();
                    requestFocusInWindow();
                });
            }
        });

        // Mostra e richiedi il focus
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            toFront();
            requestFocusInWindow();
        });
    }

    public void addListener(KeyListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(KeyListener l) {
        listeners.remove(l);
    }

    public void disposeWindow() {
        listeners.clear();
        dispose();
    }
}
