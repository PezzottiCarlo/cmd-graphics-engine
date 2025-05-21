// File: src/ch/carlopezzotti/Main.java
package ch.carlopezzotti;

import ch.carlopezzotti.engine.*;
import ch.carlopezzotti.engine.helper.Vector3;
import ch.carlopezzotti.engine.helper.Transform;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main implements KeyCaptureWindow.KeyListener {
    private volatile boolean w, a, s, d;
    private volatile boolean left, rightt;

    private static final double MOVE_SPEED = 10.0;
    private static final double ROT_SPEED  = Math.PI;
    private static Engine engine;
    private static List<Node> enemies;
    private static final Random rnd = new Random();

    public static void main(String[] args) throws Exception {
        Main app = new Main();
        SwingUtilities.invokeLater(() -> {
            KeyCaptureWindow kcw = new KeyCaptureWindow();
            kcw.addListener(app);
            System.out.println("WASD muove, frecce ruotano, SPAZIO: teleporta nemico più vicino a destra");
        });
        Thread.sleep(100);
        app.runEngine();
    }

    private void runEngine() throws IOException {
        int width = 120, height = 100, fps = 30;
        engine = new Engine(width, height, fps, new BrailleDisplay());
        Camera cam = engine.getCamera();
        cam.setFov(60);
        cam.setDist(10);

        TreeScene scene = engine.getScene();
        enemies = loadSimpleScene(scene, engine);

        engine.onRender((g, w_, h_, delta) -> updateCamera(cam, delta));
        engine.start();
    }

    private static ArrayList<Node> loadSimpleScene(TreeScene scene, Engine engine) throws IOException {
        String PATH = "src/main/resources/scene/";
        String[] OBJ = { "cow.obj", "pistol.obj","newscene.obj" };
        ArrayList<Node> list = new ArrayList<>();

        for (String of : OBJ) {
            List<double[]> vs = new ArrayList<>();
            List<int[]> fs    = new ArrayList<>();
            for (String line : Files.readAllLines(Paths.get(PATH + of))) {
                if (line.startsWith("v ")) {
                    String[] t = line.split("\\s+");
                    vs.add(new double[]{
                        Double.parseDouble(t[1]),
                        Double.parseDouble(t[2]),
                        Double.parseDouble(t[3])
                    });
                } else if (line.startsWith("f ")) {
                    String[] t = line.split("\\s+");
                    fs.add(new int[]{
                        Integer.parseInt(t[1].split("/")[0]) - 1,
                        Integer.parseInt(t[2].split("/")[0]) - 1,
                        Integer.parseInt(t[3].split("/")[0]) - 1
                    });
                }
            }
            Node n = new Node(of.replace(".obj",""));
            n.setVertices(vs);
            n.setFaces(fs);
            list.add(n);
        }

        // configure cow enemy
        Node cow = list.get(0);
        cow.setLocalPosition(randomPos());
        cow.setColor(Engine.Color.RED);
        scene.addNode(cow);
        // auto‐wander
        cow.setLocalRotation(new Vector3((float)Math.PI,0, 0));
        cow.startAutoUpdate(100, n -> {
            Vector3 p = n.getLocalPosition();
            Vector3 delta = new Vector3(
                (rnd.nextFloat()*5-1)*0.5f,
                0,
                (rnd.nextFloat()*5-1)*0.5f
            );
            n.setLocalPosition(p.add(delta));
        });

        // camera and pistol
        Camera cam = engine.getCamera();
        scene.addNode(cam);

        Node pistol = list.get(1);
        cam.addChild(pistol);
        pistol.setInheritRotation(true,false,false);
        pistol.setLocalPosition(new Vector3(-4,3,-5));
        pistol.setLocalRotation(new Vector3((float)Math.PI,(float)Math.PI,0));
        pistol.setColor(Engine.Color.BLACK);

        return list;  // contains cow
    }

    private static Vector3 randomPos() {
        return new Vector3(
            (rnd.nextFloat()*2-1)*10,  // x ∈ [−10,10]
            0,
            (rnd.nextFloat()*2-1)*10   // z ∈ [−10,10]
        );
    }

    private void updateCamera(Camera cam, double delta) {
        Vector3 pos = cam.getLocalPosition();
        Vector3 rot = cam.getLocalRotation();
        Vector3 forward = new Vector3(0,0,1).rotateY((float)rot.y);
        Vector3 right   = new Vector3(1,0,0).rotateY((float)rot.y);

        if (w) pos = pos.add( forward.mul((float)(MOVE_SPEED*delta)));
        if (s) pos = pos.sub( forward.mul((float)(MOVE_SPEED*delta)));
        if (d) pos = pos.add( right.mul((float)(MOVE_SPEED*delta)));
        if (a) pos = pos.sub( right.mul((float)(MOVE_SPEED*delta)));
        cam.setLocalPosition(pos);

        if (left)   cam.rotateY((float)(-ROT_SPEED*delta));
        if (rightt) cam.rotateY((float)( ROT_SPEED*delta));

        Vector3 r2 = cam.getLocalRotation();
        double mp = Math.toRadians(89), cp = Math.max(-mp, Math.min(mp, r2.x));
        cam.setLocalRotation(new Vector3((float)cp,(float)r2.y,(float)r2.z));
    }

    @Override
    public void onKeyDown(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: w=true; break;
            case KeyEvent.VK_A: a=true; break;
            case KeyEvent.VK_S: s=true; break;
            case KeyEvent.VK_D: d=true; break;
            case KeyEvent.VK_LEFT:  left=true; break;
            case KeyEvent.VK_RIGHT: rightt=true; break;
            case KeyEvent.VK_SPACE: relocateNearestRight(); break;
        }
    }

    private void relocateNearestRight() {
        Camera cam = engine.getCamera();
        Transform ct = cam.getGlobalTransform();
        Vector3 camPos   = ct.position;
        Vector3 camRight = new Vector3(1,0,0)
            .rotateZ((float)ct.rotation.z)
            .rotateY((float)ct.rotation.y)
            .rotateX((float)ct.rotation.x);

        Node best = null;
        double bestDist = Double.POSITIVE_INFINITY;
        for (Node enemy : enemies) {
            Vector3 epos = enemy.getGlobalTransform().position;
            Vector3 diff = epos.sub(camPos);
            if (diff.dot(camRight) <= 0) continue;
            double d2 = diff.dot(diff);
            if (d2 < bestDist) {
                bestDist = d2;
                best = enemy;
            }
        }
        if (best != null) {
            best.setLocalPosition(randomPos());
            System.out.println("Mucca uccisa");
        }
    }

    @Override
    public void onKeyUp(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: w=false; break;
            case KeyEvent.VK_A: a=false; break;
            case KeyEvent.VK_S: s=false; break;
            case KeyEvent.VK_D: d=false; break;
            case KeyEvent.VK_LEFT:  left=false; break;
            case KeyEvent.VK_RIGHT: rightt=false; break;
        }
    }
}
