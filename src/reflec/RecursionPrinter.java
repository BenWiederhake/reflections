package reflec;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import reflec.DisplayPanel.Afterpainter;
import reflec.MutableModel.ModelListener;

public final class RecursionPrinter implements Afterpainter, ModelListener {
    private static final int FONTSIZE = 12;

    private static final int OFFSET_X = 2;

    private static final int OFFSET_Y = 18;

    private static final Font FONT =
        new Font(Font.MONOSPACED, Font.PLAIN, FONTSIZE);

    private static final Color FONT_COLOR = new Color(192, 192, 192);

    private static final Color BACKGROUND_FILL = new Color(0, 0, 0, 128);

    private final DisplayPanel panel;

    private final DefaultMutableModel model;

    private RecursionPrinter(final DisplayPanel panel,
    final DefaultMutableModel model) {
        this.panel = panel;
        this.model = model;
    }

    @Override
    public void paint(final Graphics2D g, final PaintConfig config,
    final PainterFactory fac) {
        final String toDisplay = String.format("Recursion level: %d",
            Integer.valueOf(model.getDepth()));

        g.setFont(FONT);
        final Rectangle2D bounds =
            g.getFontMetrics().getStringBounds(toDisplay, g);
        g.setColor(BACKGROUND_FILL);
        bounds.add(OFFSET_X, OFFSET_Y);
        g.fill(bounds);
        g.setColor(FONT_COLOR);
        g.drawString(toDisplay, OFFSET_X, OFFSET_Y);
    }

    @Override
    public void notifyModelChanged(final boolean coreModelChanged) {
        if (!coreModelChanged) {
            /*
             * Repaint events are never necessary, but if depth is changed, be
             * extra sure.
             */
            panel.repaint();
        }
    }

    public static void hookInto(final DisplayPanel panel,
    final DefaultMutableModel model) {
        final RecursionPrinter l = new RecursionPrinter(panel, model);
        panel.addAfterpainter(l);
        model.add(l);
    }
}
