package reflec;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.UnmodifiableIterator;

public final class SweepHelper {
    private static final boolean DEBUG = false;

    private static final Comparator<ImmutablePoint> POINT_COMPARATOR =
        new Comparator<ImmutablePoint>() {
            @Override
            public int compare(
            final ImmutablePoint o1, final ImmutablePoint o2) {
                final int resX = Double.compare(o1.x, o2.x);
                if (resX != 0) {
                    return resX;
                }
                return Double.compare(o1.y, o2.y);
            }
        };

    private static final Comparator<SweepEvent> EVENT_COMPARATOR =
        new Comparator<SweepEvent>() {
            @Override
            public int compare(final SweepEvent o1, final SweepEvent o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (null == o1) {
                    return -1;
                }
                if (null == o2) {
                    return 1;
                }

                return ComparisonChain.start()
                    .compare(o1.angle, o2.angle)
                    .compare(o1.type, o2.type)
                    .compare(o1.which, o2.which)
                    .compare(o1.l.p1, o2.l.p1, POINT_COMPARATOR)
                    .compare(o1.l.p2, o2.l.p2, POINT_COMPARATOR)
                    .result();
            }
        };

    private static final SweepEventIterator EMPTY = new SweepEventIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public SweepEvent next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<ImmutableLine> getOpenSet() {
            return Collections.emptySet();
        }
    };

    private final SortedSet<SweepEvent> set = new TreeSet<>(EVENT_COMPARATOR);

    private final Set<ImmutableLine> open = new LinkedHashSet<>();

    private final ImmutablePoint source;

    private final SweepEvent start;

    private boolean built /* = false */;

    public SweepHelper(final ImmutablePoint source) {
        this.source = source;
        start = null;
    }

    public SweepHelper(final ImmutablePoint source,
    final ImmutablePoint start) {
        this.source = source;
        this.start = SweepEvent.create(start, source);
    }

    public SweepHelper(final ImmutablePoint source, final ImmutableLine start) {
        this.source = source;

        final SweepEvent[] pair = new SweepEvent[2];
        SweepEvent.insert(Objects.requireNonNull(start), pair, source);
        this.start = pair[0];
    }

    public void prepare(final Iterable<ImmutableLine> lines) {
        Preconditions.checkState(!built);
        final SweepEvent[] pair = new SweepEvent[2];

        for (final ImmutableLine line : lines) {
            SweepEvent.insert(line, pair, source);
            set.add(pair[0]);
            set.add(pair[1]);
            /* if (pair[0] < gateBegin < pair[1]) // (but ON A CIRCLE) */
            if (ordered(pair[0], start, pair[1])) {
                open.add(line);
            }
        }
    }

    public void prepare(final ImmutablePoint point) {
        Preconditions.checkState(!built);
        set.add(SweepEvent.create(point, source));
    }

    public SweepEventIterator build() {
        Preconditions.checkState(!built);
        built = true;

        if (set.isEmpty()) {
            return EMPTY;
        }

        final SortedSet<SweepEvent> tailSet = set.tailSet(start);
        if (DEBUG) {
            System.out.println("SweepHelper.build(): set=" + set
                + ", starting with " + tailSet
                + ", open=" + open);
        }
        return new DefaultIterator(set, open, tailSet.iterator());
    }

    private boolean ordered(
    final SweepEvent begin, final SweepEvent middle, final SweepEvent end) {
        if (EVENT_COMPARATOR.compare(begin, end) < 0) {
            /*
             * Normal order. Behave as if we can cut the circle at 0 without
             * breaking the begin->end segment:
             * 
             * return (begin < middle < end); // Ternary <
             */
            return EVENT_COMPARATOR.compare(begin, middle) < 0
                && EVENT_COMPARATOR.compare(middle, end) < 0;
        }

        /*
         * Inverted order: Behave as if we can cut the circle at 0 without
         * breaking the end->begin segment, then just apply logic:
         * 
         * return !(end <= middle <= begin); // Ternary <=
         * 
         * return (end > middle) || (middle > begin);
         */
        return EVENT_COMPARATOR.compare(end, middle) > 0
            || EVENT_COMPARATOR.compare(middle, begin) > 0;
    }

    public interface SweepEventIterator
    extends Iterator<SweepEvent> {
        Set<ImmutableLine> getOpenSet();
    }

    private static final class DefaultIterator
    extends UnmodifiableIterator<SweepEvent> implements SweepEventIterator {
        private final SortedSet<SweepEvent> events;

        private final Set<ImmutableLine> open;

        private Iterator<SweepEvent> iter;

        public DefaultIterator(final SortedSet<SweepEvent> events,
        final Set<ImmutableLine> open, final Iterator<SweepEvent> iter) {
            this.events = events;
            this.open = open;
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            if (!iter.hasNext()) {
                iter = events.iterator();
            }
            return iter.hasNext();
        }

        @Override
        public SweepEvent next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final SweepEvent next = iter.next();
            switch (next.type) {
                case BEGIN:
                    open.add(next.l);
                    break;
                case END:
                    open.remove(next.l);
                    break;
                case POINT:
                    break;
                default:
                    throw new InternalError();
            }
            return next;
        }

        @Override
        public Set<ImmutableLine> getOpenSet() {
            /*
             * Once inserted / added, there is a clearly defined order on the
             * open set (visibility order from source).
             * 
             * TODO: Implement this order for efficiency? (O(n^2) -> O(n log
             * n)))
             */
            return Collections.unmodifiableSet(open);
        }
    }
}
