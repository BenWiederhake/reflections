package reflec;

import java.awt.geom.Point2D;
import java.util.Collection;

public final class ImmutablePoint extends Point2D.Double {
    /** Not meant for serialization. */
    private static final long serialVersionUID = 1L;

    public ImmutablePoint(final Point2D.Double from) {
        this.x = from.getX();
        this.y = from.getY();
    }

    public ImmutablePoint(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public double angle(final ImmutablePoint that) {
        return Math.atan2(that.y - this.y, that.x - this.x);
    }

    @Override
    public void setLocation(final double x, final double y) {
        throw new UnsupportedOperationException();
    }

    public String toRawString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public String toString() {
        return "ImmutablePoint" + toRawString();
    }

    public ImmutableLine computeNearest(
    final ImmutablePoint direction, final Collection<ImmutableLine> lines) {
        if (lines.isEmpty()) {
            return null;
        }

        final ImmutableLine dirLine = new ImmutableLine(this, direction);

        ImmutableLine best = null;
        double sqBestDist = java.lang.Double.POSITIVE_INFINITY;

        /* TODO: Avoid iterating over SweepIterator#openSet */
        final Point2D.Double buf = new Point2D.Double();
        for (final ImmutableLine line : lines) {
            dirLine.getIntersection(line, buf);
            final double sqBufDist = this.distanceSq(buf);

            if (sqBufDist < sqBestDist) {
                sqBestDist = sqBufDist;
                best = line;
            }
        }

        /* Note: Can be null when lines are parallel. */
        return best;
    }
}
