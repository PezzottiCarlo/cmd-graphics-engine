package ch.carlopezzotti.engine.helper;

public class Vector3 {
    public float x, y, z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    public Vector3(Vector2 v, float z) {
        this.x = v.x;
        this.y = v.y;
        this.z = z;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 sub(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 mul(float s) {
        return new Vector3(x * s, y * s, z * s);
    }

    public Vector3 div(float s) {
        return new Vector3(x / s, y / s, z / s);
    }

    public Vector3 cross(Vector3 v) {
        return new Vector3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x);
    }

    public float dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3 normalize() {
        float len = length();
        if (len == 0)
            return new Vector3(0, 0, 0);
        return new Vector3(x / len, y / len, z / len);
    }

    public Vector3 rotateX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Vector3(
                x,
                y * cos - z * sin,
                y * sin + z * cos);
    }

    public Vector3 rotateY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Vector3(
                x * cos + z * sin,
                y,
                -x * sin + z * cos);
    }

    public Vector3 rotateZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Vector3(
                x * cos - y * sin,
                x * sin + y * cos,
                z);
    }

    public Vector3 rotate(float angleX, float angleY, float angleZ) {
        return rotateX(angleX).rotateY(angleY).rotateZ(angleZ);
    }
}