package reflec;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public final class MyTestPanel extends JPanel {
    /** Not meant for serialization. */
    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND = Color.BLACK;

    private static final Color OUTLINE = Color.RED;

    private static final Color FILLING = Color.GREEN;

    public MyTestPanel() {
        /* Nothing to do here */
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;

        g.setColor(BACKGROUND);
        g2d.fillRect(0, 0, Integer.MIN_VALUE, Integer.MIN_VALUE);
        g2d.fillRect(0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        g2d.fillRect(0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
        g2d.fillRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        g.fillRect(Integer.MIN_VALUE, Integer.MIN_VALUE,
            Integer.MAX_VALUE, Integer.MAX_VALUE);
        g.setColor(FILLING);

        final Path2D path = new Path2D.Double();
        path.moveTo(50, 50);
        path.lineTo(75, 40);
        path.lineTo(125, 60);
        path.lineTo(150, 50);
        path.moveTo(100, 100);
        path.lineTo(75, 110);
        path.lineTo(50, 100);
        path.closePath();

        g2d.fill(path);
        g.setColor(OUTLINE);
        g2d.draw(path);
    }

    public static void main(final String[] args) {
        final JFrame win = new JFrame("Hello");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        win.setContentPane(new MyTestPanel());
        win.setSize(800, 600);
        win.setVisible(true);
    }
}
