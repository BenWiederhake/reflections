package reflec;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

public final class DepthScroller implements MouseWheelListener {
    private static final int MAX_DEPTH = 20;

    private final DefaultMutableModel model;

    private DepthScroller(final DefaultMutableModel model) {
        this.model = model;
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            final int delta = e.getUnitsToScroll() < 0 ? -1 : 1;
            final int newDepth = model.getDepth() + delta;
            model.setDepth(Math.min(MAX_DEPTH, Math.max(-1, newDepth)));
        } else {
            System.err.println("WHEEL_BLOCK not supported"
                + " (What is that anyway?).");
        }
    }

    public static void hookInto(
    final DefaultMutableModel model, final JComponent component) {
        component.addMouseWheelListener(new DepthScroller(model));
    }
}
