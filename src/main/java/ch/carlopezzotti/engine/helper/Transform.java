package ch.carlopezzotti.engine.helper;

public class Transform {
    public final Vector3 position;
    public final Vector3 rotation;
    public final Vector3 scale;

    public Transform(Vector3 position, Vector3 rotation, Vector3 scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Transform combine(Transform t) {
        Vector3 sp = t.position.mul((float) scale.x)
                .rotateZ((float) rotation.z)
                .rotateY((float) rotation.y)
                .rotateX((float) rotation.x);
        Vector3 newPos = position.add(sp);
        Vector3 newRot = rotation.add(t.rotation);
        Vector3 newScale = scale.mul((float) t.scale.x);
        return new Transform(newPos, newRot, newScale);
    }

    public Vector3 apply(Vector3 v) {
        Vector3 vs = v.mul((float) scale.x);
        Vector3 vr = vs.rotateX((float) rotation.x)
                .rotateY((float) rotation.y)
                .rotateZ((float) rotation.z);
        return vr.add(position);
    }
}
