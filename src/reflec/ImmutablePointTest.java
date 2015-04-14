package reflec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImmutablePointTest {
    private static final double DELTA = 0.000000001;

    private static final double EPSILON = 0.000000000000001;

    private static final double SHIFT_X = 10;

    private static final double SHIFT_Y = 30;

    private static final double SCALE = 10;

    public ImmutablePointTest() {
        /* Nothing to do here */
    }

    @Test
    public void testAngle() {
        final ImmutablePoint center = new ImmutablePoint(SHIFT_X, SHIFT_Y);

        final double[][] points = new double[][] {
            {
                -1, -EPSILON, Math.PI * (-4) / 4.0,
            },
            {
                -1, -1, Math.PI * (-3) / 4.0,
            },
            {
                +0, -1, Math.PI * (-2) / 4.0,
            },
            {
                +1, -1, Math.PI * (-1) / 4.0,
            },
            {
                +1, +0, Math.PI * (+0) / 4.0,
            },
            {
                +1, +1, Math.PI * (+1) / 4.0,
            },
            {
                +0, +1, Math.PI * (+2) / 4.0,
            },
            {
                -1, +1, Math.PI * (+3) / 4.0,
            },
            {
                -1, +EPSILON, Math.PI * (+4) / 4.0,
            },
        };

        for (final double[] point : points) {
            final ImmutablePoint p = new ImmutablePoint(
                SHIFT_X + SCALE * point[0],
                SHIFT_Y + SCALE * point[1]);
            assertEquals(point[2], center.angle(p), DELTA);
        }
    }
}
