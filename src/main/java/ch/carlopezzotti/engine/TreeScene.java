package ch.carlopezzotti.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import ch.carlopezzotti.engine.Engine.Graphics;
import ch.carlopezzotti.engine.helper.Transform;
import ch.carlopezzotti.engine.helper.Vector3;

public class TreeScene {
    private final List<Node> nodes = new ArrayList<>();

    public void addNode(Node node) {
        if (!nodes.contains(node)) nodes.add(node);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
        for (Node c : node.getChildren()) removeNode(c);
    }

    public void renderAll(Engine engine, Graphics g, int w, int h) {
        List<Node> sorted = new ArrayList<>(nodes);
        Vector3 camPos = engine.getCamera().getGlobalTransform().position;
        sorted.sort(Comparator.comparingDouble(n -> {
            Vector3 pos = n.getGlobalTransform().position;
            double dx = pos.x - camPos.x;
            double dy = pos.y - camPos.y;
            double dz = pos.z - camPos.z;
            return -(dx*dx + dy*dy + dz*dz);
        }));
        for (Node n : sorted) renderNode(engine, g, w, h, n);
    }

    private void renderNode(Engine engine, Graphics g, int w, int h, Node n) {
        g.setColor(n.getColor());
        for (int[] f : n.getFaces()) {
            int[] p1 = n.projectVertex(engine, n.getVertices().get(f[0]));
            int[] p2 = n.projectVertex(engine, n.getVertices().get(f[1]));
            int[] p3 = n.projectVertex(engine, n.getVertices().get(f[2]));
            if (p1 != null && p2 != null && p3 != null)
                g.fillTriangle(p1[0], p1[1], p2[0], p2[1], p3[0], p3[1]);
        }
        for (Node c : n.getChildren()) renderNode(engine, g, w, h, c);
    }
}