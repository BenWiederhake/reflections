package reflec;

import java.awt.geom.Point2D;
import java.util.Objects;

import reflec.SweepHelper.SweepEventIterator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public final class ReflectedGroup extends AbstractRayGroup {
    private static final boolean DEBUG = false;

    private static final Point2D.Double BUF = new Point2D.Double();

    private final AbstractRayGroup parent;

    private final ImmutablePoint source;

    private final ImmutableLine ignore;

    public ReflectedGroup(final Model model, final AbstractRayGroup parent,
    final ImmutablePoint source, final ImmutableLine gate,
    final ImmutableLine ignore) {
        super(model, parent.getReflectionLevel() + 1, source, gate);

        this.parent = parent;
        this.source = source;
        this.ignore = Objects.requireNonNull(ignore);

        if (DEBUG) {
            System.out.format(toString());
        }
    }

    @Override
    public ReflectionResult doReflection() {
        final SweepEventIterator iter = startSweep();

        final CollectHelper collector =
            new CollectHelper(model, this, iter, source, getGate().p2);
        collector.start();

        boolean breakOut = false;
        while (!breakOut) {
            final SweepEvent ev = iter.next();

            switch (ev.type) {
                case BEGIN:
                    if (collector.isVisible(ev)) {
                        collector.startSegment(ev);
                    }
                    break;

                case END:
                    if (collector.isVisible(ev)) {
                        collector.endSegment(ev.p);
                    }
                    break;

                case POINT:
                    if (model.getSink() == ev.p) {
                        if (collector.isVisible(ev)) {
                            collector.setRay(ev.p);
                        }
                    } else if (getGate().p1 == ev.p) {
                        /* We're done */
                        collector.endSegment(ev.p);
                        breakOut = true;
                    } else {
                        throw new InternalError();
                    }
                    break;

                default:
                    throw new InternalError();
            }
        }

        return collector.build();
    }

    private SweepEventIterator startSweep() {
        final SweepEventIterator iter;

        final SweepHelper builder = new SweepHelper(source, getGate());

        if (DEBUG) {
            System.out.println("ReflectedGroup.startSweep()"
                + ": NEW SESSION by " + source + " through gate " + getGate());
        }
        final Iterable<ImmutableLine> filtered =
            Iterables.filter(model.getMirrors(),
                new GateFilter());
        /*
         * Note: Even if .filtered is empty now, we still might be interested in
         * the order of model.getSink() vs. getGate().p2
         */
        builder.prepare(filtered);

        if (getGate().relativeCCW(model.getSink()) <= 0) {
            builder.prepare(model.getSink());
        }

        builder.prepare(getGate().p1);
        iter = builder.build();

        return iter;
    }

    @Override
    public Ray.Builder pathTo(final ImmutablePoint dst) {
        intersection2buf(dst);
        final ImmutablePoint reflecPoint = new ImmutablePoint(BUF);
        final Ray.Builder builder = parent.pathTo(reflecPoint);
        builder.add(dst);
        return builder;
    }

    private void intersection2buf(final ImmutablePoint dst) {
        ignore.getIntersection(source.x, source.y, dst.x, dst.y, BUF);
    }

    @Override
    public Beam toBeam() {
        return parent.getCoveredAreaBuilder(getGate()).build();
    }

    @Override
    public String toString() {
        return String.format("ReflectedGroup.ReflectedGroup(): %s%n"
            + "\tparent: %s%n"
            + "\tsource: %s%n"
            + "\tgate: %s%n"
            + "\tignore: %s%n",
            toIdentString(),
            parent.toIdentString(),
            source.toRawString(),
            getGate().toRawString(),
            ignore.toRawString());
    }

    @Override
    protected Beam.Builder getCoveredAreaBuilder(final ImmutableLine via) {
        intersection2buf(via.p1);
        final ImmutablePoint inter1 = new ImmutablePoint(BUF);
        intersection2buf(via.p2);
        final ImmutablePoint inter2 = new ImmutablePoint(BUF);

        /* Must change order */
        final ImmutableLine reflVia = new ImmutableLine(inter2, inter1);
        final Beam.Builder ret = parent.getCoveredAreaBuilder(reflVia);
        ret.add(via, source);
        return ret;
    }

    private final class GateFilter implements Predicate<ImmutableLine> {
        public GateFilter() {
            /* Nothing to do here */
        }

        @Override
        public boolean apply(final ImmutableLine l) {
            if (DEBUG) {
                System.out.println("ReflectedGroup.startSweep()"
                    + ".Predicate.apply(): Testing " + l);
            }
            // return ignore != l
            // && (getGate().relativeCCW(l.p1) <= 0
            // || getGate().relativeCCW(l.p2) <= 0);
            if (ignore == l) {
                if (DEBUG) {
                    System.out.println("\tfalse ('== ignore')");
                }
                return false;
            }
            ImmutablePoint p1 = l.p1;
            boolean p1Vis = getGate().relativeCCW(p1) <= 0;
            ImmutablePoint p2 = l.p2;
            boolean p2Vis = getGate().relativeCCW(p2) <= 0;

            if (p1Vis == p2Vis) {
                if (DEBUG) {
                    System.out.println(
                        "\t" + p1Vis + " ('start agree')");
                }
                return p1Vis;
            }

            /*
             * It crosses the gate, at least numerically, so we
             * actually have to analyze the situation:
             */
            {
                final ImmutableLine sightStart =
                    new ImmutableLine(source, getGate().p1);
                final boolean p1AfterStart =
                    sightStart.relativeCCW(p1) >= 0;
                final boolean p2AfterStart =
                    sightStart.relativeCCW(p2) >= 0;

                if (!p1AfterStart && !p2AfterStart) {
                    /* both not visible */
                    if (DEBUG) {
                        System.out.println(
                            "\tfalse ('before start')");
                    }
                    return false;
                }
                /*
                 * If they intersect sightStart, then cut l down to
                 * the visible part
                 */
                if (p1AfterStart && !p2AfterStart) {
                    sightStart.getIntersection(l, BUF);
                    p2 = new ImmutablePoint(BUF);
                    p2Vis = getGate().relativeCCW(p2) <= 0;
                    if (p1Vis == p2Vis) {
                        if (DEBUG) {
                            System.out.println("\t" + p1Vis
                                + " ('startcut1 agree')");
                        }
                        return p1Vis;
                    }
                } else if (!p1AfterStart && p2AfterStart) {
                    sightStart.getIntersection(l, BUF);
                    p1 = new ImmutablePoint(BUF);
                    p1Vis = getGate().relativeCCW(p1) <= 0;

                    if (p1Vis == p2Vis) {
                        if (DEBUG) {
                            System.out.println("\t" + p1Vis
                                + " ('startcut2 agree')");
                        }
                        return p1Vis;
                    }
                }
                /* doesn't cross sightStart anymore */
            }
            {
                final ImmutableLine sightEnd =
                    new ImmutableLine(source, getGate().p2);
                final boolean p1AfterEnd =
                    sightEnd.relativeCCW(p1) >= 0;
                final boolean p2AfterEnd =
                    sightEnd.relativeCCW(p2) >= 0;

                if (p1AfterEnd && p2AfterEnd) {
                    /* both not visible */
                    if (DEBUG) {
                        System.out.println("\tfalse ('after end')");
                    }
                    return false;
                }
                /*
                 * If they intersect sightEnd, then cut
                 * "whats left of l" down to the visible part
                 */
                if (!p1AfterEnd && p2AfterEnd) {
                    sightEnd.getIntersection(l, BUF);
                    p2 = new ImmutablePoint(BUF);
                    p2Vis = getGate().relativeCCW(p2) <= 0;
                    if (p1Vis == p2Vis) {
                        if (DEBUG) {
                            System.out.println("\t"
                                + p1Vis
                                + " ('endcut1 agree')");
                        }
                        return p1Vis;
                    }
                } else if (p1AfterEnd && !p2AfterEnd) {
                    sightEnd.getIntersection(l, BUF);
                    p1 = new ImmutablePoint(BUF);
                    p1Vis = getGate().relativeCCW(p1) <= 0;

                    if (p1Vis == p2Vis) {
                        if (DEBUG) {
                            System.out.println("\t"
                                + p1Vis
                                + " ('endcut2 agree')");
                        }
                        return p1Vis;
                    }
                }
                /* doesn't cross sightEnd anymore */
            }

            /*
             * Now the line p1->p2 is wholly in sight, but it still
             * crosses gate. This shouldn't happen, so simply assume
             * that either of the endpoint just barely slipped over
             * the edge, and use the mid to determine it:
             */
            BUF.x = (p1.x + p2.x) / 2;
            BUF.y = (p1.y + p2.y) / 2;
            final boolean ret = getGate().relativeCCW(BUF) <= 0;
            if (DEBUG) {
                System.out.println("\t" + ret + " ('final')");
            }
            return ret;
        }
    }
}
