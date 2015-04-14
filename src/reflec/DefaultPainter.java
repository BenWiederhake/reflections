package reflec;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Random;

import reflec.Beam.RenderedBeam;

import com.google.common.base.Preconditions;

public final class DefaultPainter implements PainterFactory.Painter {
    private static final Color BACKGROUND_COL = Color.BLACK;

    private static final Color SOURCE_COL = Color.ORANGE;

    private static final double SOURCE_RADIUS = 5;

    private static final Color SINK_COL = Color.RED;

    private static final double SINK_RADIUS = 3;

    private static final Color MIRROR_COL = new Color(128, 128, 255, 255);

    private static final Color RAY_MIN_COL = new Color(0, 255, 128, 192);

    private static final Color RAY_MAX_COL = new Color(32, 255, 255, 192);

    private static final Color GROUP_COL = new Color(255, 255, 64, 64);

    private static final Color GROUP_OUTLINE_COL =
        new Color(255, 255, 128, 255);

    private final Point2D.Double ptBuf = new Point2D.Double();

    private final Line2D.Double lBuf = new Line2D.Double();

    private final Graphics2D g2d;

    private final AffineTransform transform;

    private final Random rand = new Random();

    private final boolean paintBeams;

    public DefaultPainter(final PaintConfig config, final Graphics2D outer,
    final boolean paintBeams) {
        this.paintBeams = paintBeams;
        g2d = (Graphics2D) outer.create();
        transform = config.getTransform();
    }

    @Override
    public void clear() {
        g2d.setColor(BACKGROUND_COL);
        g2d.fillRect(0, 0, Integer.MIN_VALUE, Integer.MIN_VALUE);
        g2d.fillRect(0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        g2d.fillRect(0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
        g2d.fillRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void drawSource(final ImmutablePoint p) {
        g2d.setColor(SOURCE_COL);
        drawCircle(p, SOURCE_RADIUS);
    }

    @Override
    public void drawSink(final ImmutablePoint p) {
        g2d.setColor(SINK_COL);
        drawCircle(p, SINK_RADIUS);
    }

    private void drawCircle(final ImmutablePoint p, final double r) {
        transform.transform(p, ptBuf);
        /* TODO: Buffer object? */
        g2d.fill(new Ellipse2D.Double(
            ptBuf.x - r,
            ptBuf.y - r,
            1 + 2 * r,
            1 + 2 * r));
    }

    @Override
    public void drawMirror(final ImmutableLine l) {
        g2d.setColor(MIRROR_COL);
        drawLine(l);
    }

    private void drawLine(final ImmutableLine l) {
        transform.transform(l.p1, ptBuf);
        lBuf.x1 = ptBuf.x;
        lBuf.y1 = ptBuf.y;
        transform.transform(l.p2, ptBuf);
        lBuf.x2 = ptBuf.x;
        lBuf.y2 = ptBuf.y;
        g2d.draw(lBuf);
    }

    @Override
    public void drawRay(final Ray r) {
        g2d.setColor(rayColor(r));

        transform.transform(r.points.get(0), ptBuf);
        lBuf.x1 = ptBuf.x;
        lBuf.y1 = ptBuf.y;

        final int nPoints = r.points.size();
        for (int i = 1; i < nPoints; ++i) {
            transform.transform(r.points.get(i), ptBuf);
            if ((i % 2) != 0) {
                lBuf.x2 = ptBuf.x;
                lBuf.y2 = ptBuf.y;
            } else {
                lBuf.x1 = ptBuf.x;
                lBuf.y1 = ptBuf.y;
            }
            g2d.draw(lBuf);
        }
    }

    private Color rayColor(final Ray r) {
        rand.setSeed(r.hashCode());
        return new Color(rayColorComponent(
            RAY_MIN_COL.getRed(),
            RAY_MAX_COL.getRed()),
            rayColorComponent(RAY_MIN_COL.getGreen(), RAY_MAX_COL.getGreen()),
            rayColorComponent(RAY_MIN_COL.getBlue(), RAY_MAX_COL.getBlue()),
            255);
    }

    private int rayColorComponent(final int min, final int max) {
        Preconditions.checkArgument(max >= min);
        return min + rand.nextInt(max - min + 1);
    }

    @Override
    public void drawGroup(final RayGroup group) {
        if (!paintBeams) {
            return;
        }

        final RenderedBeam coveredArea = group.toBeam().render(transform);
        g2d.setColor(GROUP_COL);
        g2d.fill(coveredArea.area);
        g2d.setColor(GROUP_OUTLINE_COL);
        g2d.draw(coveredArea.outline);
    }

    @Override
    public void dispose() {
        g2d.dispose();
    }

    // CHECKSTYLE AbstractClassName OFF: Not that kind of factory.
    public static final class Factory implements PainterFactory {
        // CHECKSTYLE AbstractClassName ON
        private boolean paintingBeams /* = false */;

        public Factory() {
            /* Nothing to do here */
        }

        public boolean isPaintingBeams() {
            return paintingBeams;
        }

        public void setPaintingBeams(final boolean paintingBeams) {
            this.paintingBeams = paintingBeams;
        }

        @Override
        public Painter start(final PaintConfig config,
        final Graphics2D onto) {
            return new DefaultPainter(config, onto, paintingBeams);
        }
    }
}
