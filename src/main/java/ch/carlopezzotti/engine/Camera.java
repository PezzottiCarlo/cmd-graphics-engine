package ch.carlopezzotti.engine;

public class Camera extends Node {
    private double fov;
    private double dist;

    public Camera(String id, double fov, double dist) {
        super(id);
        this.fov  = fov;
        this.dist = dist;
    }

    public double getFov() {
        return fov;
    }
    public void setFov(double fov) {
        this.fov = fov;
    }

    public double getDist() {
        return dist;
    }
    public void setDist(double dist) {
        this.dist = dist;
    }
}
