// File: src/ch/carlopezzotti/engine/Engine.java
package ch.carlopezzotti.engine;

import java.io.IOException;
import java.util.function.Consumer;

import ch.carlopezzotti.engine.helper.Transform;
import ch.carlopezzotti.engine.helper.Vector3;

public class Engine {
    public enum Color {
        RESET("\u001B[0m"), BLACK("\u001B[30m"), RED("\u001B[31m"),
        GREEN("\u001B[32m"), YELLOW("\u001B[33m"), BLUE("\u001B[34m"),
        MAGENTA("\u001B[35m"), CYAN("\u001B[36m"), WHITE("\u001B[37m"),
        BRIGHT_BLACK("\u001B[90m"), BRIGHT_RED("\u001B[91m"),
        BRIGHT_GREEN("\u001B[92m"), BRIGHT_YELLOW("\u001B[93m"),
        BRIGHT_BLUE("\u001B[94m"), BRIGHT_MAGENTA("\u001B[95m"),
        BRIGHT_CYAN("\u001B[96m"), BRIGHT_WHITE("\u001B[97m");

        private final String code;
        Color(String c) { code = c; }
        @Override public String toString() { return code; }
    }

    public interface Graphics {
        void setColor(Color c);
        void setPixel(int x, int y);
        void drawLine(int x1, int y1, int x2, int y2);
        default void fillTriangle(int x1,int y1,int x2,int y2,int x3,int y3){
            int minX=Math.min(x1,Math.min(x2,x3)), maxX=Math.max(x1,Math.max(x2,x3));
            int minY=Math.min(y1,Math.min(y2,y3)), maxY=Math.max(y1,Math.max(y2,y3));
            double denom=((y2-y3)*(x1-x3)+(x3-x2)*(y1-y3));
            for(int y=minY;y<=maxY;y++){
                for(int x=minX;x<=maxX;x++){
                    double a=((y2-y3)*(x-x3)+(x3-x2)*(y-y3))/denom;
                    double b=((y3-y1)*(x-x3)+(x1-x3)*(y-y3))/denom;
                    double c=1-a-b;
                    if(a>=0&&b>=0&&c>=0) setPixel(x,y);
                }
            }
        }
    }

    public interface RenderCallback {
        void render(Graphics g, int w, int h, double delta);
    }

    private final int width, height, fps;
    private final Display display;
    private final Color[][] colorBuffer;
    private final TreeScene scene;
    private final Camera camera;
    private RenderCallback renderCallback;
    private Consumer<Character> keyCallback;

    public Engine(int width, int height, int fps, Display display) {
        this.width  = width;
        this.height = height;
        this.fps    = fps;
        this.display= display;
        this.colorBuffer = new Color[height][width];
        this.scene  = new TreeScene();
        this.camera = new Camera("camera", 60.0, 100.0); // dist positivo
        this.camera.setLocalPosition(new Vector3(0, 0, 0));
        scene.addNode(camera);
    }

    public TreeScene getScene()       { return scene; }
    public Camera    getCamera()      { return camera; }
    public void onRender(RenderCallback cb) { this.renderCallback = cb; }
    public void onKey(Consumer<Character> cb) { this.keyCallback = cb; }

    public void start() {
        new Thread(this::runLoop, "Engine-Thread").start();
    }

    private void runLoop() {
        try {
            display.init();
            startInputThread();
            long last = System.nanoTime();
            long period = 1_000_000_000L / fps;

            while (true) {
                long now = System.nanoTime();
                double delta = (now - last) / 1e9;
                last = now;

                // clear
                for(int y=0;y<height;y++) for(int x=0;x<width;x++) colorBuffer[y][x]=null;

                Graphics g = new Graphics() {
                    private Color current = Color.WHITE;
                    @Override public void setColor(Color c) { current=c; System.out.print(c); }
                    @Override public void setPixel(int x,int y){
                        if(x>=0&&x<width&&y>=0&&y<height) colorBuffer[y][x]=current;
                    }
                    @Override public void drawLine(int x1,int y1,int x2,int y2){
                        int dx=Math.abs(x2-x1), sx=x1<x2?1:-1;
                        int dy=-Math.abs(y2-y1), sy=y1<y2?1:-1;
                        int err=dx+dy;
                        while(true){
                            setPixel(x1,y1);
                            if(x1==x2&&y1==y2) break;
                            int e2=2*err;
                            if(e2>=dy){err+=dy; x1+=sx;}
                            if(e2<=dx){err+=dx; y1+=sy;}
                        }
                    }
                };

                scene.renderAll(this,g,width,height);
                if(renderCallback!=null) renderCallback.render(g,width,height,delta);

                int[] flat = new int[width*height];
                Color[] cols = new Color[width*height];
                int idx=0;
                for(int y=0;y<height;y++){
                    for(int x=0;x<width;x++){
                        flat[idx] = (colorBuffer[y][x]!=null?1:0);
                        cols[idx] = colorBuffer[y][x];
                        idx++;
                    }
                }
                display.clear();
                display.draw(flat,width,height,cols);

                long elapsed=System.nanoTime()-now;
                long sleep=period-elapsed;
                if(sleep>0) Thread.sleep(sleep/1_000_000,(int)(sleep%1_000_000));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void startInputThread() {
        new Thread(() -> {
            try {
                while(true) {
                    int c = System.in.read();
                    if(c<0) break;
                    if(keyCallback!=null) keyCallback.accept((char)c);
                }
            } catch(IOException ignored){}
        },"Engine-Input").start();
    }

    public int[] project(double wx, double wy, double wz) {
        Transform camT = camera.getGlobalTransform();
        Vector3 cp = camT.position;
        Vector3 cr = camT.rotation;

        double dx = wx - cp.x,
               dy = wy - cp.y,
               dz = wz - cp.z;
        double cy = Math.cos(-cr.y),
               sy = Math.sin(-cr.y);
        double cpitch = Math.cos(-cr.x),
               sp     = Math.sin(-cr.x);
        double crl = Math.cos(-cr.z),
               sr  = Math.sin(-cr.z);

        double x1 = dx*cy + dz*sy;
        double z1 = -dx*sy + dz*cy;
        double y1 = dy*cpitch - z1*sp;
        double z2 = dy*sp + z1*cpitch;         // CLIP if behind camera

        double x2 = x1*crl - y1*sr;
        double y2 = x1*sr + y1*crl;
        double zp = z2 + camera.getDist();
        if (zp <= 1.0) return null;            // near‐plane clipping

        double scale = camera.getFov() / zp;
        int sx = (int)Math.round(x2*scale + width/2.0);
        int syy = (int)Math.round(y2*scale + height/2.0);
        return new int[]{ sx, syy };
    }
}
