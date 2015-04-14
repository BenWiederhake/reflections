package reflec;

import java.util.Objects;

public final class SweepEvent {
    public final SweepEvent.Type type;

    public final ImmutableLine l;

    public final ImmutablePoint p;

    public final double angle;

    public final int which;

    private SweepEvent(final SweepEvent.Type type, final ImmutableLine line,
    final ImmutablePoint center, final boolean useEnd) {
        this.type = Objects.requireNonNull(type);
        this.l = line;
        if (useEnd) {
            p = l.p2;
            which = 2;
        } else {
            p = l.p1;
            which = 1;
        }
        this.angle = center.angle(p);
    }

    private SweepEvent(final ImmutablePoint p, final double angle) {
        type = Type.POINT;
        l = new ImmutableLine(p, p);
        which = 0;
        this.p = p;
        this.angle = angle;
    }

    @Override
    public int hashCode() {
        return l.hashCode() + which * 17;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final SweepEvent that = (SweepEvent) obj;
        return this.l.equals(that.l) && this.which == that.which;
    }

    @Override
    public String toString() {
        return String.format("%s@%f[%s = #%d of %s]",
            type, new Double(angle), p, Integer.valueOf(which), l);
    }

    public static void insert(final ImmutableLine line,
    final SweepEvent[] into, final ImmutablePoint center) {
        final boolean swapEnds = line.relativeCCW(center) > 0;
        into[0] = new SweepEvent(Type.BEGIN, line, center, swapEnds);
        into[1] = new SweepEvent(Type.END, line, center, !swapEnds);
    }

    public static SweepEvent create(
    final ImmutablePoint interest, final ImmutablePoint center) {
        return new SweepEvent(interest, center.angle(interest));
    }

    public static enum Type {
        BEGIN, END, POINT
    }
}
