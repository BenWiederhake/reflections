package reflec;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import reflec.DisplayPanel.Afterpainter;

public final class CursorMarker
implements Afterpainter, MouseMotionListener, MouseListener {
    private static final int RADIUS = 50;

    private final DisplayPanel panel;

    private ClickState state = ClickState.INVISIBLE;

    private int x;

    private int y;

    private CursorMarker(final DisplayPanel panel) {
        this.panel = panel;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        /* Nothing to do here */
    }

    private void update(final ClickState nstate, final MouseEvent e) {
        final int nx = e.getX();
        final int ny = e.getY();
        if (nstate != state || nx != x || ny != y) {
            state = nstate;
            x = nx;
            y = ny;
            panel.repaint();
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        switch (e.getButton()) {
            case 1:
                update(ClickState.BUTTON1, e);
                break;
            case 2:
                update(ClickState.BUTTON2, e);
                break;
            case 3:
                update(ClickState.BUTTON3, e);
                break;
            default:
                update(ClickState.INVISIBLE, e);
                break;
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        update(ClickState.NORMAL, e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        update(ClickState.NORMAL, e);
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        update(ClickState.INVISIBLE, e);
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        update(state, e);
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        update(state, e);
    }

    @Override
    public void paint(final Graphics2D g,
    final PaintConfig config, final PainterFactory fac) {
        g.setColor(state.col);
        g.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
    }

    public static void hookInto(final DisplayPanel panel) {
        final CursorMarker marker = new CursorMarker(panel);
        panel.addMouseListener(marker);
        panel.addMouseMotionListener(marker);
        panel.addAfterpainter(marker);
    }

    private static enum ClickState {
        INVISIBLE(new Color(0, 0, 0, 0)),
        NORMAL(Color.BLUE),
        BUTTON1(Color.RED),
        BUTTON2(Color.YELLOW),
        BUTTON3(Color.GREEN);

        public final Color col;

        private ClickState(final Color col) {
            this.col = col;
        }
    }
}
