package ch.carlopezzotti.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import ch.carlopezzotti.engine.helper.Transform;
import ch.carlopezzotti.engine.helper.Vector3;

public class Node {
    private final String id;
    private Vector3 localPosition = new Vector3(0, 0, 0);
    private Vector3 localRotation = new Vector3(0, 0, 0);
    private Vector3 localScale    = new Vector3(1, 1, 1);
    private Engine.Color color = Engine.Color.WHITE;
    private final List<double[]> vertices = new ArrayList<>();
    private final List<int[]> faces = new ArrayList<>();
    private Node parent;
    private final List<Node> children = new ArrayList<>();

    private boolean inheritYaw   = true;
    private boolean inheritPitch = false;
    private boolean inheritRoll  = false;

    private Thread updateThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public Node(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public Vector3 getLocalPosition() { return localPosition; }
    public void setLocalPosition(Vector3 pos) { localPosition = pos; }
    public Vector3 getLocalRotation() { return localRotation; }
    public void setLocalRotation(Vector3 rot) { localRotation = rot; }
    public void rotateX(float angle) { localRotation = localRotation.add(new Vector3(angle, 0, 0)); }
    public void rotateY(float angle) { localRotation = localRotation.add(new Vector3(0, angle, 0)); }
    public void rotateZ(float angle) { localRotation = localRotation.add(new Vector3(0, 0, angle)); }
    public Vector3 getLocalScale() { return localScale; }
    public void setLocalScale(Vector3 scale) { localScale = scale; }
    public void scale(float s) { localScale = localScale.mul(s); }

    public Engine.Color getColor() { return color; }
    public void setColor(Engine.Color c) { color = c; }

    public List<double[]> getVertices() { return vertices; }
    public List<int[]> getFaces() { return faces; }
    public void addVertex(double x, double y, double z) { vertices.add(new double[]{x, y, z}); }
    public void addFace(int i1, int i2, int i3) { faces.add(new int[]{i1, i2, i3}); }
    public void setVertices(List<double[]> verts) { vertices.clear(); vertices.addAll(verts); }
    public void setFaces(List<int[]> fs) { faces.clear(); faces.addAll(fs); }

    public Node getParent() { return parent; }
    public void addChild(Node child) { child.parent = this; children.add(child); }
    public List<Node> getChildren() { return children; }

    public void setInheritRotation(boolean yaw, boolean pitch, boolean roll) {
        inheritYaw   = yaw;
        inheritPitch = pitch;
        inheritRoll  = roll;
    }

    public Transform getGlobalTransform() {
        Transform t = new Transform(localPosition, localRotation, localScale);
        if (parent != null) {
            Transform pt = parent.getGlobalTransform();
            Vector3 pr = pt.rotation;
            Vector3 filtered = new Vector3(
                inheritPitch ? pr.x : 0,
                inheritYaw   ? pr.y : 0,
                inheritRoll  ? pr.z : 0
            );
            Transform pf = new Transform(pt.position, filtered, pt.scale);
            return pf.combine(t);
        }
        return t;
    }

    public int[] projectVertex(Engine engine, double[] v) {
        Transform gt = getGlobalTransform();
        Vector3 tv = gt.apply(new Vector3((float)v[0], (float)v[1], (float)v[2]));
        return engine.project(tv.x, tv.y, tv.z);
    }

    public void startAutoUpdate(long intervalMillis, Consumer<Node> action) {
        if (running.get()) return;
        running.set(true);
        updateThread = new Thread(() -> {
            try {
                while (running.get()) {
                    long t0 = System.currentTimeMillis();
                    action.accept(this);
                    long dt = System.currentTimeMillis() - t0;
                    if (dt < intervalMillis) Thread.sleep(intervalMillis - dt);
                }
            } catch (InterruptedException ignored) {}
        }, "Node-AutoUpdate-" + id);
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void stopAutoUpdate() {
        running.set(false);
        if (updateThread != null) {
            updateThread.interrupt();
            updateThread = null;
        }
    }
}
