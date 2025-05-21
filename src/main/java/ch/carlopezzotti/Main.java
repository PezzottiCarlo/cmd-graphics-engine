// File: src/ch/carlopezzotti/Main.java
package ch.carlopezzotti;

import ch.carlopezzotti.engine.BrailleDisplay;
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
    private volatile boolean left, rightt, up, down;

    private static final double MOVE_SPEED = 10.0;
    private static final double ROT_SPEED = -Math.PI / 10;

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
        Engine engine = new Engine(width, height, fps, display);
        Engine.Camera cam = engine.getCamera();

        TreeScene scene = engine.getScene();
        loadSimpleScene(scene);

        cam.x = 0;
        cam.y = 0;
        cam.z = 90;
        cam.yaw = 0;
        cam.pitch = 0;
        cam.roll = 0;
        cam.fov = 70;
        cam.dist = 100;

        final double[] tAcc = { 0 };

        engine.onRender((Graphics g, int w_, int h_, double delta) -> {
            app.updateCamera(cam, delta);
            tAcc[0] += delta;
        });

        engine.start();
    }

    private static void loadSimpleScene(TreeScene scene) throws IOException {
        final String PATH = "src/main/resources/scene/";
        final String[] OBJ_FILES = {
                "cow.obj",
                "teapot.obj"
        };

        List<Node> nodes = new ArrayList<>();
        for (String objFile : OBJ_FILES) {
            List<double[]> verts = new ArrayList<>();
            List<int[]> faces = new ArrayList<>();
            loadOBJ(PATH + objFile, 1.0, verts, faces);
            Node node = new Node(objFile.replace(".obj", ""));
            node.setFaces(faces);
            node.setVertices(verts);
            nodes.add(node);
        }
        Node ref = nodes.get(0);
        ref.setLocalPosition(new Vector3(0, 10, 0));
        ref.setColor(Color.RED);
        Color[] rainbow = {
                Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.CYAN, Color.MAGENTA
        };
        ref.startAutoUpdate(1000, (n) -> {
            int index = 0;
            for (int i = 0; i < rainbow.length; i++) {
                if (n.getColor() == rainbow[i]) {
                    index = i;
                    break;
                }
            }
            index = (index + 1) % rainbow.length;
            n.setColor(rainbow[index]);
        });

        nodes.get(1).startAutoUpdate(10, (n) -> {
            Vector3 tmp = n.getLocalPosition();
            n.rotateX(0.01f);
        });

        nodes.forEach(scene::addNode);
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

    private void updateCamera(Engine.Camera cam, double delta) {
        Vector3 right = new Vector3(1, 0, 0).rotateZ((float) cam.yaw);

        if (a) {
            cam.x -= right.x * MOVE_SPEED * delta;
            cam.y -= right.y * MOVE_SPEED * delta;
        }
        if (d) {
            cam.x += right.x * MOVE_SPEED * delta;
            cam.y += right.y * MOVE_SPEED * delta;
        }
        if (w) {
            cam.dist = Math.max(1.0, cam.dist - MOVE_SPEED * delta);
        }
        if (s) {
            cam.dist += MOVE_SPEED * delta;
        }
        if (left)
            cam.yaw -= ROT_SPEED * delta;
        if (rightt)
            cam.yaw += ROT_SPEED * delta;
        if (up)
            cam.pitch += ROT_SPEED * delta;
        if (down)
            cam.pitch -= ROT_SPEED * delta;
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
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_DOWN:
                down = true;
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
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            case KeyEvent.VK_ESCAPE:
                System.out.print(Color.RESET);
                System.exit(0);
        }
    }
}
