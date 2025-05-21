// File: src/ch/carlopezzotti/Main.java
package ch.carlopezzotti;

import ch.carlopezzotti.engine.BrailleDisplay;
import ch.carlopezzotti.engine.Camera;
import ch.carlopezzotti.engine.Display;
import ch.carlopezzotti.engine.Engine;
import ch.carlopezzotti.engine.Engine.Color;
import ch.carlopezzotti.engine.Engine.Graphics;
import ch.carlopezzotti.engine.KeyCaptureWindow;
import ch.carlopezzotti.engine.TreeScene;
import ch.carlopezzotti.engine.Node;
import ch.carlopezzotti.engine.helper.Vector3;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main implements KeyCaptureWindow.KeyListener {
    private volatile boolean w, a, s, d;
    private volatile boolean left, rightt;

    private static final double MOVE_SPEED = 10.0;
    private static final double ROT_SPEED = Math.PI;
    private static Engine engine;

    public static void main(String[] args) throws Exception {
        Main app = new Main();

        SwingUtilities.invokeLater(() -> {
            KeyCaptureWindow kcw = new KeyCaptureWindow();
            kcw.addListener(app);
            System.out.println("KeyCaptureWindow avviata. Usa WASD e frecce per muovere camera.");
        });

        Thread.sleep(100);
        runEngine(args, app);
    }

    private static void runEngine(String[] args, Main app) throws IOException {
        final int width = 120, height = 100;
        final int fps = 30;

        Display display = new BrailleDisplay();
        engine = new Engine(width, height, fps, display);
        Camera cam = engine.getCamera();
        cam.setFov(60);
        cam.setDist(10);

        TreeScene scene = engine.getScene();
        loadSimpleScene(scene);

        final double[] tAcc = { 0 };

        engine.onRender((Graphics g, int w_, int h_, double delta) -> {
            app.updateCamera(cam, delta);
            tAcc[0] += delta;
        });

        engine.start();
    }

    private static void loadSimpleScene(TreeScene scene) throws IOException {
        final String PATH = "src/main/resources/scene/";
        final String[] OBJ_FILES = { "cow.obj", "pistol.obj" };

        // carica mucca e pistola in lista temporanea
        List<Node> nodes = new ArrayList<>();
        for (String objFile : OBJ_FILES) {
            List<double[]> verts = new ArrayList<>();
            List<int[]> faces = new ArrayList<>();
            loadOBJ(PATH + objFile, 1.0, verts, faces);

            Node node = new Node(objFile.replace(".obj", ""));
            node.setVertices(verts);
            node.setFaces(faces);
            nodes.add(node);
        }

        // PUBLIC SCENE: aggiungi solo la mucca come root
        Node cow = nodes.get(0);
        cow.setLocalPosition(new Vector3(3, 0, 0));
        cow.setLocalRotation(new Vector3((float) Math.PI, (float) Math.PI/10, Math.PI/10));
        cow.setColor(Engine.Color.RED);
        scene.addNode(cow);

        // animazione arcobaleno sulla mucca
        Color[] rainbow = {
                Engine.Color.RED, Engine.Color.GREEN, Engine.Color.BLUE,
                Engine.Color.YELLOW, Engine.Color.CYAN, Engine.Color.MAGENTA
        };
        cow.startAutoUpdate(1000, n -> {
            int idx = java.util.Arrays.asList(rainbow).indexOf(n.getColor());
            n.setColor(rainbow[(idx + 1) % rainbow.length]);
            float z = n.getLocalRotation().z;
            z = -z;
            n.setLocalRotation(new Vector3(n.getLocalRotation().x, n.getLocalRotation().y, z));
        });

        // prendi la camera e attaccala alla scena come secondo root
        Camera cam = engine.getCamera();
        scene.addNode(cam);

        // configura la pistola come figlio della camera
        Node pistol = nodes.get(1);
        cam.addChild(pistol);

        // fai ereditare solo la rotazione orizzontale (yaw)
        pistol.setInheritRotation(true, false, false);

        // posizionamento relativo alla camera
        pistol.setLocalPosition(new Vector3(-4, 3, -5));
        // ruota 180° su X e Y per allinearla alla view
        pistol.setLocalRotation(new Vector3((float) Math.PI, (float) Math.PI, 0));
        pistol.setColor(Engine.Color.BLACK);
    }

    private static void loadOBJ(String path, double scale,
            List<double[]> verts,
            List<int[]> faces) throws IOException {
        for (String line : Files.readAllLines(Paths.get(path))) {
            if (line.startsWith("v ")) {
                String[] tok = line.split("\\s+");
                double x = Double.parseDouble(tok[1]) * scale;
                double y = Double.parseDouble(tok[2]) * scale;
                double z = Double.parseDouble(tok[3]) * scale;
                verts.add(new double[] { x, y, z });
            } else if (line.startsWith("f ")) {
                String[] tok = line.split("\\s+");
                int i1 = Integer.parseInt(tok[1].split("/")[0]) - 1;
                int i2 = Integer.parseInt(tok[2].split("/")[0]) - 1;
                int i3 = Integer.parseInt(tok[3].split("/")[0]) - 1;
                faces.add(new int[] { i1, i2, i3 });
            }
        }
    }

    private void updateCamera(Camera cam, double delta) {
        // Prendi posizione e rotazione locali correnti
        Vector3 pos = cam.getLocalPosition();
        Vector3 rot = cam.getLocalRotation();

        // Vettori forward e right in base allo yaw (rotazione Y)
        Vector3 forward = new Vector3(0, 0, 1).rotateY((float) rot.y);
        Vector3 right = new Vector3(1, 0, 0).rotateY((float) rot.y);

        // Traslazioni WASD
        if (w) {
            pos = pos.add(forward.mul((float) (MOVE_SPEED * delta)));
        }
        if (s) {
            pos = pos.sub(forward.mul((float) (MOVE_SPEED * delta)));
        }
        if (d) {
            pos = pos.add(right.mul((float) (MOVE_SPEED * delta)));
        }
        if (a) {
            pos = pos.sub(right.mul((float) (MOVE_SPEED * delta)));
        }
        cam.setLocalPosition(pos);

        // Rotazioni con le frecce
        if (left)
            cam.rotateY((float) (-ROT_SPEED * delta)); // gira verso sinistra
        if (rightt)
            cam.rotateY((float) (ROT_SPEED * delta)); // verso destra

        // Clamp del pitch (rotazione X) per non capovolgere
        Vector3 newRot = cam.getLocalRotation();
        double maxPitch = Math.toRadians(89);
        double clampedPitch = Math.max(-maxPitch, Math.min(maxPitch, newRot.x));
        cam.setLocalRotation(new Vector3((float) clampedPitch, (float) newRot.y, (float) newRot.z));
    }

    @Override
    public void onKeyDown(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                w = true;
                break;
            case KeyEvent.VK_A:
                a = true;
                break;
            case KeyEvent.VK_S:
                s = true;
                break;
            case KeyEvent.VK_D:
                d = true;
                break;
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightt = true;
                break;
            case KeyEvent.VK_ESCAPE:
                System.out.print(Color.RESET);
                Camera cam = engine.getCamera();
                System.out.println("Camera position: " + cam.getLocalPosition().x + ", " + cam.getLocalPosition().y
                        + ", " + cam.getLocalPosition().z);
                // wait for 5 seconds
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
                break;
        }
    }

    @Override
    public void onKeyUp(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                w = false;
                break;
            case KeyEvent.VK_A:
                a = false;
                break;
            case KeyEvent.VK_S:
                s = false;
                break;
            case KeyEvent.VK_D:
                d = false;
                break;
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightt = false;
                break;
        }
    }
}
