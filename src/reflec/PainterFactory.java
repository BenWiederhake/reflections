package reflec;

import java.awt.Graphics2D;

public interface PainterFactory {
    Painter start(PaintConfig config, Graphics2D onto);

    public interface Painter {
        void clear();
        
        void drawSource(ImmutablePoint p);

        void drawSink(ImmutablePoint p);

        void drawMirror(ImmutableLine l);

        void drawRay(Ray ray);

        void drawGroup(RayGroup group);
        
        void dispose();
    }
}
