package reflec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImmutableLineTest {
    private static final double DELTA = 0.0000000001;

    public ImmutableLineTest() {
        /* Nothing to do here */
    }

    @Test
    public void testMirror() {
        final double[][] data = new double[][] {
            {
                -1, 0, 0, -1, 0, 1, 1, 0,
            },
            {
                -1, 0, 0, -10, 0, -9, 1, 0,
            },
            {
                -1, 0, 0, 10, 0, 9, 1, 0,
            },
            {
                -0.3926, 0, 0, 10, 0, 9, 0.3926, 0,
            },
            {
                -1, 10, 0, 10, 0, 9, 1, 10,
            },
            {
                -13, 10, 0, 10, 0, 9, 13, 10,
            },
            {
                -1, -1, 5, -5, -5, 5, 1, 1,
            },
            {
                0, -1, 5, -5, -5, 5, 1, 0,
            },
            {
                -5, -1, 0, 0, -2, 10, 5, 1,
            },
            {
                -6, 4, 0, 0, -2, 10, 4, 6,
            },
            {
                0, 0, 0, 0, -2, 10, 0, 0,
            },
            {
                0, 0, 0, 0, 1, 0, 0, 0,
            },
            {
                0, 0, 0, 0, 0, 1, 0, 0,
            },
        };
        for (final double[] set : data) {
            final ImmutablePoint source =
                new ImmutablePoint(set[0], set[1]);
            final ImmutableLine mirror =
                new ImmutableLine(set[2], set[3], set[4], set[5]);
            final ImmutablePoint destination =
                new ImmutablePoint(set[6], set[7]);

            expectMirror(source, mirror, destination);
            expectMirror(destination, mirror, source);
        }
    }

    private void expectMirror(final ImmutablePoint source,
    final ImmutableLine mirror, final ImmutablePoint expected) {
        final ImmutablePoint actual = mirror.mirror(source);
        final String errMsg = String.format(
            "%s.mirror(%s) was %s instead of %s",
            mirror.toRawString(), source.toRawString(),
            actual.toRawString(), expected.toRawString());
        assertEquals(errMsg, expected.x, actual.x, DELTA);
    }
}
