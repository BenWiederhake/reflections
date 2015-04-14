package reflec;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Beam {
    private final ImmutablePoint start;

    private final ImmutableList<ImmutableLine> lines;

    private Beam(final ImmutablePoint start,
    final ImmutableList<ImmutableLine> lines) {
        this.start = start;
        this.lines = lines;
    }

    public ImmutableList<ImmutableLine> getLines() {
        return lines;
    }

    public ImmutablePoint getStart() {
        return start;
    }

    public RenderedBeam render(final AffineTransform transform) {
        final Point2D.Double last1 = new Point2D.Double();
        final Point2D.Double last2 = new Point2D.Double();
        final Point2D.Double next1 = new Point2D.Double();
        final Point2D.Double next2 = new Point2D.Double();

        transform.transform(start, last1);
        transform.transform(lines.get(0).p1, next1);
        transform.transform(lines.get(0).p2, next2);

        final PathBuilder outline = PathBuilder.startNonzero(last1);
        outline.then(next1).move(last1).then(next2);
        final PathBuilder area = PathBuilder.startNonzero(last1);
        area.then(next1).then(next2);

        for (int i = 1; i < lines.size(); ++i) {
            last1.setLocation(next1);
            last2.setLocation(next2);
            transform.transform(lines.get(i).p1, next1);
            transform.transform(lines.get(i).p2, next2);

            outline.move(last1).then(next2).move(last2).then(next1);
            area.move(last1).then(last2).then(next1).then(next2);
        }

        return new RenderedBeam(outline.build(), area.build());
    }

    public static final class Builder {
        private static final boolean STRICT = true;

        private final ImmutablePoint start;

        private final ImmutableList.Builder<ImmutableLine> lines =
            ImmutableList.builder();

        private ImmutableLine lastLine;

        public Builder(final ImmutablePoint start,
        final ImmutableLine initial) {
            this.start = start;
            this.lastLine = initial;
            final boolean ccwOfLine = lastLine.relativeCCW(start) > 0;
            Preconditions.checkArgument(!STRICT || !ccwOfLine);
            lines.add(initial);
        }

        public void add(
        final ImmutableLine next, final ImmutablePoint prevSource) {
            final boolean ccwOfGate = next.relativeCCW(prevSource) > 0;
            final boolean ccwOfParentGate =
                lastLine.relativeCCW(prevSource) >= 0;
            /* This is crucial! */
            Preconditions.checkArgument(!STRICT || !ccwOfGate);
            Preconditions.checkArgument(!STRICT || ccwOfParentGate);
            lines.add(next);
            lastLine = next;
        }

        public Beam build() {
            return new Beam(start, lines.build());
        }
    }

    public static final class RenderedBeam {
        public final Path2D outline;

        public final Path2D area;

        private RenderedBeam(final Path2D outline, final Path2D area) {
            this.outline = outline;
            this.area = area;
        }
    }
}
