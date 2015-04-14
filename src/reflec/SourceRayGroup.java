package reflec;

import reflec.SweepHelper.SweepEventIterator;

import com.google.common.collect.ImmutableList;

public final class SourceRayGroup extends AbstractRayGroup {
    public SourceRayGroup(final Model model) {
        super(model, 0, model.getSource(),
            new ImmutableLine(model.getSource(), model.getSource()));
    }

    @Override
    public ReflectionResult doReflection() {
        if (model.getMirrors().isEmpty()) {
            return new ReflectionResult(
                ImmutableList.<RayGroup> of(),
                pathTo(model.getSink()).build());
        }

        final SweepEventIterator iter;
        {
            final SweepHelper builder = new SweepHelper(model.getSource());
            builder.prepare(model.getMirrors());
            builder.prepare(model.getSink());
            iter = builder.build();
        }

        final CollectHelper collector;
        {
            /*
             * We need a direction, so make one step:
             */
            final SweepEvent ev = iter.next();
            collector =
                new CollectHelper(model, this, iter, model.getSource(), ev.p);
            /* TODO: Poison for collector#activeStart? */
        }

        /*
         * Don't unnecessarily break a segment, so start at the first *visible*
         * event
         */
        SweepEvent firstDigested = null;

        while (true) {
            final SweepEvent ev = iter.next();
            final boolean wasNull = null == firstDigested;

            switch (ev.type) {
                case BEGIN:
                    if (collector.isVisible(ev)) {
                        collector.startSegment(ev);
                        if (null == firstDigested) {
                            firstDigested = ev;
                            collector.start();
                        }
                    }
                    break;

                case END:
                    if (collector.isVisible(ev)) {
                        collector.endSegment(ev.p);
                        if (null == firstDigested) {
                            firstDigested = ev;
                            collector.start();
                        }
                    }
                    break;

                case POINT:
                    if (model.getSink() == ev.p) {
                        if (null != firstDigested && collector.isVisible(ev)) {
                            collector.setRay(ev.p);
                        }
                    } else {
                        throw new InternalError();
                    }
                    break;

                default:
                    throw new InternalError();
            }

            if (!wasNull && ev == firstDigested) {
                /*
                 * Sigh. Why is "do {boolean foo = complexStuff();} while(foo);"
                 * not allowed?!
                 */
                break;
            }
        }

        return collector.build();
    }

    @Override
    public Ray.Builder pathTo(final ImmutablePoint dst) {
        return Ray.builder(model.getSource(), dst);
    }

    @Override
    protected Beam.Builder getCoveredAreaBuilder(final ImmutableLine via) {
        return new Beam.Builder(model.getSource(), via);
    }

    @Override
    public Beam toBeam() {
        return new Beam.Builder(model.getSource(), getGate()).build();
    }

    public static boolean onCcwSideOf(
    final ImmutablePoint p, final ImmutableLine line) {
        return line.relativeCCW(p) >= 0;
    }
}
