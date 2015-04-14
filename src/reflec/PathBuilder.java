package reflec;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import com.google.common.base.Preconditions;

public final class PathBuilder {
    private final Path2D path = new Path2D.Double();

    private boolean built /* = false */;

    private PathBuilder(final Point2D.Double start, final int windingRule) {
        path.setWindingRule(windingRule);
        path.moveTo(start.x, start.y);
    }

    public PathBuilder move(final Point2D.Double then) {
        Preconditions.checkState(!built);
        path.closePath();
        path.moveTo(then.x, then.y);
        return this;
    }

    public PathBuilder then(final Point2D.Double then) {
        Preconditions.checkState(!built);
        path.lineTo(then.x, then.y);
        return this;
    }

    public Path2D build() {
        Preconditions.checkState(!built);
        built = true;
        return path;
    }

    public static PathBuilder startNonzero(final Point2D.Double start) {
        return new PathBuilder(start, Path2D.WIND_NON_ZERO);
    }

    public static PathBuilder startEvenOdd(final Point2D.Double start) {
        return new PathBuilder(start, Path2D.WIND_EVEN_ODD);
    }
}
