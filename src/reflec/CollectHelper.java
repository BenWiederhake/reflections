package reflec;

import java.awt.geom.Point2D;

import reflec.RayGroup.ReflectionResult;
import reflec.SweepHelper.SweepEventIterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class CollectHelper {
    private static final double ROUNDING_ERROR_FACTOR =
        100 / Math.sqrt(Math.ulp(1));

    private final Model model;

    private final AbstractRayGroup parent;

    private final SweepEventIterator iter;

    private final ImmutablePoint source;

    private final ImmutableList.Builder<RayGroup> builder =
        ImmutableList.builder();

    private ImmutableLine active;

    private ImmutablePoint activeStart;

    private boolean onCcwOfActive;

    private Ray pathFound;

    private State state = State.INITIALIZING;

    public CollectHelper(final Model model, final AbstractRayGroup parent,
    final SweepEventIterator iter,
    final ImmutablePoint source, final ImmutablePoint start) {
        this.model = model;
        this.parent = parent;
        this.iter = iter;
        this.source = source;

        updateActive(start);
        onCcwOfActive = null == active || onCcwSideOfActive(source);
    }

    public void start() {
        Preconditions.checkState(state == State.INITIALIZING);
        state = State.STARTED;
    }

    public boolean isVisible(final SweepEvent ev) {
        Preconditions.checkState(state != State.BUILT);
        if (null == active) {
            /*
             * 1: When there's no active line, there's nothing obstructing the
             * view anyway
             */
            return true;
        }

        /* 2: An event FROM the active line must never be ignored */
        if (ev.l == active) {
            return true;
        }

        switch (ev.type) {
            case BEGIN:
                /*
                 * Proof by exhaustive search, and then simplified the resulting
                 * truth table
                 */

                final boolean acmBeforeEl =
                    (ev.l.relativeCCW(active.mid) >= 0)
                    == (ev.l.relativeCCW(source) >= 0);
                final boolean elmBeforeActive =
                    onCcwSideOfActive(ev.l.mid) == onCcwOfActive;
                if (acmBeforeEl != elmBeforeActive) {
                    return elmBeforeActive;
                }
                return onCcwSideOfActive(ev.p) == onCcwOfActive;

            case END:
                /*
                 * Not the only END point we would be interested in (see 2), so
                 * ignore
                 */
                return false;

            case POINT:
                return onCcwSideOfActive(ev.p) == onCcwOfActive;

            default:
                throw new InternalError();
        }
    }

    private boolean onCcwSideOfActive(final ImmutablePoint p) {
        Preconditions.checkState(state != State.BUILT);
        /* active mustn't be null when entering here */
        Preconditions.checkState(null != active);
        return active.relativeCCW(p) >= 0;
    }

    private void addSegment(final ImmutablePoint givenGateEnd,
    final boolean needsProjection) {
        final ImmutablePoint gateEnd;
        if (needsProjection) {
            final Point2D.Double buf = new Point2D.Double();
            active
                .getIntersection(new ImmutableLine(source, givenGateEnd), buf);
            gateEnd = new ImmutablePoint(buf);
        } else {
            gateEnd = givenGateEnd;
            final double ptSegDist = active.ptSegDist(gateEnd);
            if (ptSegDist > 0.0001) {
                System.out.format(
                    "CollectHelper.addSegment() Suspicious needs-no-projection"
                        + " of %s onto %s (distance %f to segment,"
                        + " distance %f to line)%n",
                    givenGateEnd.toRawString(),
                    active.toRawString(),
                    Double.valueOf(ptSegDist),
                    Double.valueOf(active.ptLineDist(gateEnd)));
            }
        }

        final ImmutableLine gate = new ImmutableLine(activeStart, gateEnd);

        final double gateLength = activeStart.distance(gateEnd);
        final double maxUlp = Math.max(
            Math.max(Math.ulp(activeStart.x), Math.ulp(activeStart.y)),
            Math.max(Math.ulp(gateEnd.x), Math.ulp(gateEnd.y)));
        if (gateLength >= maxUlp * ROUNDING_ERROR_FACTOR) {
            builder.add(new ReflectedGroup(model, parent,
                active.mirror(source), gate, active));
            /* Otherwise, drop it */
        }
    }

    public void startSegment(final SweepEvent ev) {
        Preconditions.checkState(state != State.BUILT);
        if (ev.l == active) {
            /* Merge */
            return;
        }
        if (State.STARTED == state && null != active) {
            addSegment(ev.p, true);
        }
        activeStart = ev.p;
        active = ev.l;
        onCcwOfActive = null == active || onCcwSideOfActive(source);
    }

    public void endSegment(final ImmutablePoint p) {
        Preconditions.checkState(state != State.BUILT);
        /*
         * Note: Since computeNearest() can return null "unexpectedly", .active
         * might be null in corner cases (e.g. mirror running exactly through
         * the source). That's why the check.
         */
        if (State.STARTED == state && null != active) {
            addSegment(p, !(p == active.p1 || p == active.p2));
        }
        updateActive(p);
        onCcwOfActive = null == active || onCcwSideOfActive(source);
    }

    private void updateActive(final ImmutablePoint p) {
        Preconditions.checkNotNull(p);
        active = source.computeNearest(p, iter.getOpenSet());
        if (null == active) {
            activeStart = null;
        } else {
            /* TODO Meh, suboptimal. */
            final Point2D.Double buf = new Point2D.Double();
            active.getIntersection(new ImmutableLine(source, p), buf);
            activeStart = new ImmutablePoint(buf);
        }
    }

    public void setRay(final ImmutablePoint p) {
        Preconditions.checkState(
            state != State.BUILT
                && null == pathFound);
        pathFound = parent.pathTo(p).build();
    }

    public ReflectionResult build() {
        Preconditions.checkState(state != State.BUILT);
        state = State.BUILT;
        return new ReflectionResult(builder.build(), pathFound);
    }

    private static enum State {
        INITIALIZING, STARTED, BUILT
    }
}
