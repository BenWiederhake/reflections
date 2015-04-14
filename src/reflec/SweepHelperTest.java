package reflec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import reflec.SweepEvent.Type;
import reflec.SweepHelper.SweepEventIterator;

import com.google.common.collect.ImmutableSet;

public final class SweepHelperTest {
    private static final ImmutablePoint ORIGIN = new ImmutablePoint(0, 0);

    public SweepHelperTest() {
        /* Nothing to do here */
    }

    @Test
    public void testEmpty() {
        final SweepEventIterator iter1 =
            sweep(Collections.<ImmutableLine> emptySet());
        assertTrue(!iter1.hasNext());

        final SweepEventIterator iter2 =
            sweep(Collections.<ImmutableLine> emptySet(),
                new ImmutableLine(1, 1, 2, 1));
        assertTrue(!iter2.hasNext());
    }

    @Test
    public void testSimpleNoGate() {
        // TODO: Fix arrays in tests
        final ImmutableLine line = new ImmutableLine(1, -1, 2, -1);
        final ImmutableSet<ImmutableLine> lineSet = ImmutableSet.of(line);
        final SweepEventIterator iter = sweep(lineSet);
        assertTrue(iter.hasNext());

        assertEquals(Collections.emptySet(), iter.getOpenSet());

        final SweepEvent ev1 = iter.next();
        assertSame(line, ev1.l);
        assertSame(line.p1, ev1.p);
        assertSame(Type.BEGIN, ev1.type);
        assertEquals(lineSet, iter.getOpenSet());

        final SweepEvent ev2 = iter.next();
        assertSame(line, ev2.l);
        assertSame(line.p2, ev2.p);
        assertSame(Type.END, ev2.type);
        assertEquals(Collections.emptySet(), iter.getOpenSet());

        final SweepEvent ev3 = iter.next();
        assertSame(ev1, ev3);
    }

    @Test
    public void testArcingNoGate() {
        final ImmutableLine line = new ImmutableLine(-1, -1, 1, -1);
        final ImmutableSet<ImmutableLine> lineSet = ImmutableSet.of(line);
        final SweepEventIterator iter = sweep(lineSet);
        assertTrue(iter.hasNext());

        assertEquals(Collections.emptySet(), iter.getOpenSet());

        final SweepEvent ev1 = iter.next();
        assertSame(line, ev1.l);
        assertSame(line.p1, ev1.p);
        assertSame(Type.BEGIN, ev1.type);
        assertEquals(lineSet, iter.getOpenSet());

        final SweepEvent ev2 = iter.next();
        assertSame(line, ev2.l);
        assertSame(line.p2, ev2.p);
        assertSame(Type.END, ev2.type);
        assertEquals(Collections.emptySet(), iter.getOpenSet());

        final SweepEvent ev3 = iter.next();
        assertSame(ev1, ev3);
    }

    private static SweepEventIterator sweep(final Set<ImmutableLine> lines) {
        final SweepHelper helper = new SweepHelper(ORIGIN);
        helper.prepare(lines);
        return helper.build();
    }

    private static SweepEventIterator sweep(
    final Set<ImmutableLine> lines, final ImmutableLine start) {
        final SweepHelper helper = new SweepHelper(ORIGIN, start);
        helper.prepare(lines);
        return helper.build();
    }
}
