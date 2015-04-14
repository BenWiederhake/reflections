package reflec;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.google.common.base.Preconditions;

public final class PaintConfig {
    public static final PaintConfig CLIP_NONE =
        new PaintConfig(1, 1, new AffineTransform());

    public static final int DEFAULT_PADDING = 5;

    private final int width;

    private final int height;

    private final AffineTransform transform;

    private PaintConfig(final int width, final int height,
    final AffineTransform transform) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        this.width = width;
        this.height = height;
        this.transform = transform;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public AffineTransform getTransform() {
        return new AffineTransform(transform);
    }

    public static Builder builder(final Model model) {
        return new Builder(model);
    }

    public static final class Builder {
        private final Rectangle2D.Double bounds;

        private AffineTransform base;

        private int padding = DEFAULT_PADDING;

        public Builder(final Model model) {
            bounds = new Rectangle2D.Double(
                model.getSource().x, model.getSource().y, 0, 0);
            bounds.add(model.getSink());

            for (final ImmutableLine line : model.getMirrors()) {
                bounds.add(line.p1);
                bounds.add(line.p2);
            }
            bounds.width = Math.max(Double.MIN_NORMAL, bounds.width);
            bounds.height = Math.max(Double.MIN_NORMAL, bounds.height);
        }

        public void setPadding(final int padding) {
            this.padding = padding;
        }

        public void add(final Point2D p) {
            bounds.add(p);
        }

        public void setBase(final AffineTransform base) {
            this.base = new AffineTransform(base);
        }

        public PaintConfig buildFor(final int width, final int height) {
            Preconditions.checkArgument(width > 0);
            Preconditions.checkArgument(height > 0);
            final AffineTransform ret = null == base
                ? new AffineTransform()
                : new AffineTransform(base);

            ret.translate(padding, padding);

            final int availableWidth = Math.max(0, width - 2 * padding);
            final double scaleX = availableWidth / bounds.width;
            final int availableHeight = Math.max(0, height - 2 * padding);
            final double scaleY = availableHeight / bounds.height;

            if (scaleX >= scaleY) {
                /*
                 * When scaling up, we will hit the top and bottom edges of the
                 * final image faster than the left and right edges, so only
                 * actually scale by scaleY, and add margin on the left and
                 * right.
                 */
                final double scale = scaleY;

                final double halfMargin =
                    (availableWidth - bounds.width * scale) / 2;
                ret.translate(halfMargin, 0);
                ret.scale(scale, scale);
            } else {
                final double scale = scaleX;

                final double halfMargin =
                    (availableHeight - bounds.height * scale) / 2;
                ret.translate(0, halfMargin);
                ret.scale(scale, scale);
            }

            ret.translate(-bounds.x, -bounds.y);

            return new PaintConfig(width, height, ret);
        }
    }
}
