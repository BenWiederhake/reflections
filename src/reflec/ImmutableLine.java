package reflec;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public final class ImmutableLine extends Line2D {
    private static final double EPSILON = 0.0001;

    public final ImmutablePoint p1;

    public final ImmutablePoint mid;

    public final ImmutablePoint p2;

    public ImmutableLine(final double x1, final double y1,
    final double x2, final double y2) {
        this(new ImmutablePoint(x1, y1), new ImmutablePoint(x2, y2));
    }

    public ImmutableLine(final ImmutablePoint p1, final ImmutablePoint p2) {
        this.p1 = Objects.requireNonNull(p1);
        this.p2 = Objects.requireNonNull(p2);
        mid = new ImmutablePoint((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(
            Math.min(p1.x, p2.x), Math.min(p1.y, p2.y),
            Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
    }

    @Override
    public double getX1() {
        return p1.x;
    }

    @Override
    public double getY1() {
        return p1.y;
    }

    @Override
    public ImmutablePoint getP1() {
        return p1;
    }

    @Override
    public double getX2() {
        return p2.x;
    }

    @Override
    public double getY2() {
        return p2.y;
    }

    @Override
    public ImmutablePoint getP2() {
        return p2;
    }

    public ImmutablePoint mirror(final ImmutablePoint orig) {
        final boolean ccwSide = relativeCCW(orig) > 0;
        final double dist = ptLineDist(orig);
        final double factor = (2 * dist) / p1.distance(p2);

        final double relX = -(p2.y - p1.y) * factor;
        final double relY = (p2.x - p1.x) * factor;

        final ImmutablePoint ret;
        if (ccwSide) {
            ret = new ImmutablePoint(orig.x + relX, orig.y + relY);
        } else {
            ret = new ImmutablePoint(orig.x - relX, orig.y - relY);
        }
        return ret;
    }

    @Override
    public void setLine(final double x1, final double y1,
    final double x2, final double y2) {
        throw new UnsupportedOperationException();
    }

    public String toRawString() {
        return "[" + p1.toRawString() + "->" + p2.toRawString() + "]";
    }

    @Override
    public String toString() {
        return "ImmutableLine" + toRawString();
    }

    @Override
    public int hashCode() {
        return p1.hashCode() ^ p2.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ImmutableLine that = (ImmutableLine) obj;
        return this.p1.equals(that.p1)
            && this.p2.equals(that.p2);
    }

    public void getIntersection(final ImmutableLine with, final Point2D into) {
        getIntersection(with.p1.x, with.p1.y, with.p2.x, with.p2.y, into);
    }

    public void getIntersection(final double x1, final double y1,
    final double x2, final double y2, final Point2D into) {
        final double x3 = p1.x;
        final double y3 = p1.y;
        final double x4 = p2.x;
        final double y4 = p2.y;
        // CHECKSTYLE LineLength OFF Can't break a link.
        /*
         * Using
         * https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Mathematics
         * as retrieved on 2014-03-12
         */
        // CHECKSTYLE LineLength ON
        final double p1diffX = x1 - x2;
        final double p2diffX = x3 - x4;
        final double p1diffY = y1 - y2;
        final double p2diffY = y3 - y4;
        final double denominator = p1diffX * p2diffY - p1diffY * p2diffX;

        if (Math.abs(denominator) >= EPSILON) {
            final double p1cross = x1 * y2 - y1 * x2;
            final double p2cross = x3 * y4 - y3 * x4;
            final double numeratorX = p1cross * p2diffX - p1diffX * p2cross;
            final double numeratorY = p1cross * p2diffY - p1diffY * p2cross;
            into.setLocation(
                numeratorX / denominator, numeratorY / denominator);
            /* Roughly 22 flops. TODO: Optimize this? */
        } else {
            System.err.format(
                "ImmutableLine.getIntersection(): Parallel lines found?"
                    + "%n\t%s and [(%f,%f)->(%f,%f)]"
                    + " result in denominator %f%n",
                toString(),
                new java.lang.Double(x1), new java.lang.Double(y1),
                new java.lang.Double(x2), new java.lang.Double(y2),
                new java.lang.Double(denominator));
            into.setLocation(java.lang.Double.NaN, java.lang.Double.NaN);
        }
    }
}
