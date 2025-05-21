// File: src/ch/carlopezzotti/engine/Node.java
package ch.carlopezzotti.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import ch.carlopezzotti.engine.helper.Transform;
import ch.carlopezzotti.engine.helper.Vector3;

/**
 * Rappresenta un oggetto nella scena: ha id, posizione locale, colore,
 * vertici, facce, parent e children. Calcola posizione globale ricorsivamente.
 * In più può avviare un thread interno per il proprio aggiornamento continuo.
 */
public class Node {
    private final String id;
    private Vector3 localPosition = new Vector3(0, 0, 0);
    private Vector3 localRotation = new Vector3(0, 0, 0); // Euler angles in radians
    private Vector3 localScale    = new Vector3(1, 1, 1);
    private Engine.Color color = Engine.Color.WHITE;
    private final List<double[]> vertices = new ArrayList<>();
    private final List<int[]> faces = new ArrayList<>();
    private Node parent;
    private final List<Node> children = new ArrayList<>();
    
    // auto‐update support
    private Thread updateThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public Node(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    // Trasformazioni locali
    public Vector3 getLocalPosition() { return localPosition; }
    public void setLocalPosition(Vector3 pos) { this.localPosition = pos; }

    public Vector3 getLocalRotation() { return localRotation; }
    /** Imposta gli angoli di rotazione (in radianti) su X,Y,Z */
    public void setLocalRotation(Vector3 rot) { this.localRotation = rot; }
    /** Ruota localmente attorno all'asse X di angle radianti */
    public void rotateX(float angle) { this.localRotation = this.localRotation.add(new Vector3(angle, 0, 0)); }
    /** Ruota localmente attorno all'asse Y di angle radianti */
    public void rotateY(float angle) { this.localRotation = this.localRotation.add(new Vector3(0, angle, 0)); }
    /** Ruota localmente attorno all'asse Z di angle radianti */
    public void rotateZ(float angle) { this.localRotation = this.localRotation.add(new Vector3(0, 0, angle)); }

    public Vector3 getLocalScale() { return localScale; }
    /** Imposta la scala locale (x,y,z) */
    public void setLocalScale(Vector3 scale) { this.localScale = scale; }
    /** Applica una scala uniforme */
    public void scale(float s) { this.localScale = this.localScale.mul(s); }

    public Engine.Color getColor() { return color; }
    public void setColor(Engine.Color c) { this.color = c; }

    // Geometria
    public List<double[]> getVertices() { return vertices; }
    public List<int[]>    getFaces()    { return faces; }
    public void addVertex(double x, double y, double z) { vertices.add(new double[]{x, y, z}); }
    public void addFace(int i1, int i2, int i3) { faces.add(new int[]{i1, i2, i3}); }
    public void setVertices(List<double[]> vertices) { this.vertices.clear(); this.vertices.addAll(vertices); }
    public void setFaces(List<int[]> faces) { this.faces.clear(); this.faces.addAll(faces); }

    // Gerarchia
    public Node getParent() { return parent; }
    public void addChild(Node child) {
        child.parent = this;
        this.children.add(child);
    }
    public List<Node> getChildren() { return children; }

    /** Restituisce la matrice di trasformazione globale come combinazione ricorsiva */
    public Transform getGlobalTransform() {
        Transform t = new Transform(localPosition, localRotation, localScale);
        if (parent != null) {
            Transform pt = parent.getGlobalTransform();
            return pt.combine(t);
        }
        return t;
    }

    /** Utility: applica la trasformazione globale a un vertice locale */
    public int[] projectVertex(Engine engine, double[] v) {
        Transform gt = getGlobalTransform();
        Vector3 tv = gt.apply(new Vector3((float)v[0], (float)v[1], (float)v[2]));
        return engine.project(tv.x, tv.y, tv.z);
    }

    // Auto‐update
    public void startAutoUpdate(long intervalMillis, Consumer<Node> updateAction) {
        if (running.get()) return;
        running.set(true);
        updateThread = new Thread(() -> {
            try {
                while (running.get()) {
                    long t0 = System.currentTimeMillis();
                    updateAction.accept(this);
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