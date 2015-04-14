package reflec;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import reflec.DisplayPanel.Afterpainter;

public final class MouseInteractivity
implements Afterpainter, MouseListener, MouseMotionListener {
    private static final double TOUCH_RADUIS = 15;

    private static final double TOUCH_RADUIS_SQ = TOUCH_RADUIS * TOUCH_RADUIS;

    private static final Color HOVER_OUTLINE = new Color(0, 0, 255, 128);

    private static final Color HOVER_FILL = new Color(128, 128, 255, 64);

    private final Point2D.Double ptBuf = new Point2D.Double();

    private final DefaultMutableModel mutModel;

    private final DisplayPanel panel;

    private HoverType type = HoverType.NONE;

    private int mouseX;

    private int mouseY;

    private boolean dragging;

    private MouseInteractivity(final DefaultMutableModel model,
    final DisplayPanel panel) {
        this.mutModel = model;
        this.panel = panel;
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (dragging && type != HoverType.NONE) {
            final AffineTransform tr = transform();
            /* We don't use shear, so avoid the unnecessary overhead */
            /*
             * TODO: scaling on-the-fly means dragging isn't
             * transitive/commutative. Remember "original" scale?!
             */
            final double diffX = (e.getX() - mouseX) / tr.getScaleX();
            final double diffY = (e.getY() - mouseY) / tr.getScaleY();
            mouseX = e.getX();
            mouseY = e.getY();

            final ImmutablePoint newSource;
            final ImmutablePoint newSink;
            final Model oldModel = mutModel.getModel();
            if (HoverType.SINK == type) {
                newSink = new ImmutablePoint(
                    oldModel.getSink().x + diffX,
                    oldModel.getSink().y + diffY);
                newSource = oldModel.getSource();
            } else {
                newSource = new ImmutablePoint(
                    oldModel.getSource().x + diffX,
                    oldModel.getSource().y + diffY);
                newSink = oldModel.getSink();
            }
            final Model newModel =
                new Model(oldModel.getMirrors(), newSource, newSink);
            mutModel.setModel(newModel);
        }
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();

        final Model model = mutModel.getModel();

        if (touching(model.getSource())) {
            setType(HoverType.SOURCE);
        } else if (touching(model.getSink())) {
            setType(HoverType.SINK);
        } else {
            setType(HoverType.NONE);
        }
    }

    private boolean touching(final ImmutablePoint point) {
        transform().transform(point, ptBuf);
        return ptBuf.distanceSq(mouseX, mouseY) <= TOUCH_RADUIS_SQ;
    }

    private void setType(final HoverType type) {
        dragging = false;
        if (type != this.type) {
            this.type = type;
            panel.repaint();
        }
    }

    private AffineTransform transform() {
        return panel.getLastConfig().getTransform();
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        /* Nothing to do here */
        /*
         * However, for debugging, output current source and sink location on
         * right-click
         */
        if (e.getButton() == MouseEvent.BUTTON3) {
            System.out.format("Model overview: %s%n"
                + "\tmirrors: %s%n"
                + "\trays: %s%n"
                + "\tgroups:%s%n",
                mutModel.getModel(),
                mutModel.getModel().getMirrors(),
                mutModel.getRays(),
                mutModel.getGroups());
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            dragging = false;
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        if (HoverType.NONE == type) {
            mouseMoved(e);
        }
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        /* Nothing to do here */
    }

    @Override
    public void paint(final Graphics2D g,
    final PaintConfig config, final PainterFactory fac) {
        if (HoverType.NONE == type) {
            return;
        }

        final ImmutablePoint p;
        if (HoverType.SOURCE == type) {
            p = mutModel.getModel().getSource();
        } else {
            p = mutModel.getModel().getSink();
        }
        transform().transform(p, ptBuf);

        /* TODO: Buffer object? */
        final Ellipse2D.Double ellipse = new Ellipse2D.Double(
            ptBuf.x - TOUCH_RADUIS, ptBuf.y - TOUCH_RADUIS,
            2 * TOUCH_RADUIS, 2 * TOUCH_RADUIS);
        g.setColor(HOVER_FILL);
        g.fill(ellipse);
        g.setColor(HOVER_OUTLINE);
        g.draw(ellipse);
    }

    public static void hookInto(
    final DefaultMutableModel model, final DisplayPanel panel) {
        final MouseInteractivity l = new MouseInteractivity(model, panel);
        panel.addAfterpainter(l);
        panel.addMouseListener(l);
        panel.addMouseMotionListener(l);
    }

    private static enum HoverType {
        NONE, SOURCE, SINK
    }
}
