package reflec;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;

import reflec.Buffer.CancelStatus;
import reflec.Buffer.Function;
import reflec.Buffer.OutputListener;
import reflec.MutableModel.ModelListener;
import reflec.PainterFactory.Painter;

import com.google.common.collect.ImmutableList;

public final class DisplayPanel extends JPanel
implements ModelListener {
    /** Not meant for serialization. */
    private static final long serialVersionUID = 1L;

    private static final BufferedImage EMPTY_IMG =
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    private static final DisplayState EMPTY =
        new DisplayState(EMPTY_IMG, PaintConfig.CLIP_NONE);

    private final Buffer<DisplayDescription, DisplayState> buffer;

    private final PainterFactory fac;

    private final MutableModel model;

    private final List<Afterpainter> afterpainters = new LinkedList<>();

    private DisplayState lastState;

    public DisplayPanel(final PainterFactory fac,
    final Buffer.Factory bufFactory, final MutableModel model) {
        this.fac = Objects.requireNonNull(fac);
        this.model = Objects.requireNonNull(model);

        final BufferHandler l = new BufferHandler();

        this.buffer = bufFactory.create(l, l, null, EMPTY);
        addComponentListener(new ResizeHandler());
    }

    public boolean addAfterpainter(final Afterpainter pe) {
        repaint();
        return afterpainters.add(pe);
    }

    public boolean remove(final Afterpainter p) {
        return afterpainters.remove(p);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        final BufferedImage img = lastState.img;
        final int offX = (getWidth() - img.getWidth()) / 2;
        final int offY = (getHeight() - img.getHeight()) / 2;

        g.drawImage(img, offX, offY, null);

        if (!afterpainters.isEmpty()) {
            if (img.getWidth() != getWidth()
                || img.getHeight() != getHeight()) {
                g.translate(offX, offY);
                g.clipRect(0, 0, img.getWidth(), img.getHeight());
            }

            final Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            for (final Afterpainter p : afterpainters) {
                p.paint(g2d, lastState.config, fac);
            }
        }
    }

    @Override
    public void notifyModelChanged(final boolean hardReset) {
        buffer.adjust(describe(), hardReset);
    }

    public PaintConfig getLastConfig() {
        return lastState.config;
    }

    private DisplayDescription describe() {
        return new DisplayDescription(model.getModel(),
            model.getGroups(), model.getRays(),
            getWidth(), getHeight());
    }

    public interface Afterpainter {
        void paint(final Graphics2D g,
        final PaintConfig config, final PainterFactory fac);
    }

    private static final class DisplayDescription {
        public final Model model;

        public final ImmutableList<RayGroup> groups;

        public final ImmutableList<Ray> rays;

        public final int width;

        public final int height;

        public DisplayDescription(final Model model,
        final ImmutableList<RayGroup> groups,
        final ImmutableList<Ray> rays,
        final int width, final int height) {
            this.model = model;
            this.groups = groups;
            this.rays = rays;
            this.width = width;
            this.height = height;
        }
    }

    private static final class DisplayState {
        public final BufferedImage img;

        public final PaintConfig config;

        public DisplayState(final BufferedImage img, final PaintConfig config) {
            this.img = img;
            this.config = config;
        }
    }

    private final class ResizeHandler implements ComponentListener {
        public ResizeHandler() {
            /*
             * Nothing to do here. Note that we need the implicit magic to store
             * the reference to the enclosing instance, so this class can't be
             * made static.
             */
        }

        @Override
        public void componentResized(final ComponentEvent e) {
            notifyModelChanged(false);
        }

        @Override
        public void componentMoved(final ComponentEvent e) {
            /* Nothing to do here */
        }

        @Override
        public void componentShown(final ComponentEvent e) {
            /* Nothing to do here */
        }

        @Override
        public void componentHidden(final ComponentEvent e) {
            /* Nothing to do here */
        }
    }

    private final class BufferHandler implements
    OutputListener<DisplayState>, Function<DisplayDescription, DisplayState> {
        public BufferHandler() {
            /* Nothing to do here */
        }

        @Override
        public void update(final DisplayState result) {
            lastState = result;
            repaint();
        }

        @Override
        public DisplayState apply(
        final DisplayDescription in, final CancelStatus status) {
            /* Check status! */
            if (null == in) {
                return EMPTY;
            }

            final PaintConfig config =
                PaintConfig.builder(in.model).buildFor(in.width, in.height);
            final BufferedImage img =
                new BufferedImage(config.getWidth(), config.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            final Graphics2D outer = img.createGraphics();
            outer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            final Painter p = fac.start(config, outer);

            /* Model */
            for (final ImmutableLine mirr : in.model.getMirrors()) {
                p.drawMirror(mirr);
            }
            p.drawSource(in.model.getSource());
            p.drawSink(in.model.getSink());

            /* RayGroups */
            for (final RayGroup gr : in.groups) {
                p.drawGroup(gr);
            }

            /* Rays */
            for (final Ray r : in.rays) {
                p.drawRay(r);
            }

            return new DisplayState(img, config);
        }
    }
}
