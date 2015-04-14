package reflec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Arrays;

import org.junit.Test;

import reflec.Beam.RenderedBeam;
import reflec.RayGroup.ReflectionResult;

public final class SourceRayGroupTest {
    private static final Model DEFAULT_MODEL = Model.defaultModel();

    private static final double DELTA_EPSILON = 0.0001;

    private static final double DELTA_IDENTITY = -1;

    public SourceRayGroupTest() {
        /* Nothing to do here */
    }

    @Test
    public void testDoReflection() {
        final RayGroup group = initialGroup();
        final ReflectionResult result = group.doReflection();

        assertEquals(2, result.getGroups().size());

        assertTrue(null != result.getFoundRay());
        assertEquals(2, result.getFoundRay().points.size());
        assertLine(
            new ImmutableLine(
                DEFAULT_MODEL.getSource(),
                DEFAULT_MODEL.getSink()),
            new ImmutableLine(
                result.getFoundRay().points.get(0),
                result.getFoundRay().points.get(1)),
            DELTA_EPSILON);
    }

    private void assertLine(final ImmutableLine expected,
    final ImmutableLine actual, final double delta) {
        final String errMsg = String.format("%s != %s", expected, actual);
        assertEquals(errMsg, expected.p1.x, actual.p1.x, delta);
        assertEquals(errMsg, expected.p1.y, actual.p1.y, delta);
        assertEquals(errMsg, expected.p2.x, actual.p2.x, delta);
        assertEquals(errMsg, expected.p2.y, actual.p2.y, delta);
    }

    public static void assertIterator(
    final PathIterator iter, final ImmutablePoint... points) {
        final double[] expect = new double[2];
        boolean going = false;
        for (int i = 0; i < points.length; ++i) {
            if (null != points[i]) {
                expect[0] = points[i].x;
                expect[1] = points[i].y;
                assertStep(iter,
                    going ? PathIterator.SEG_LINETO : PathIterator.SEG_MOVETO,
                    expect);
                going = true;
            } else {
                assertStep(iter, PathIterator.SEG_CLOSE, new double[0]);
                going = false;
            }
        }
        assertDone(iter);
    }

    public static void assertStep(
    final PathIterator iter, final int type, final double[] expected) {
        assertTrue(!iter.isDone());
        final double[] arr = new double[6];
        Arrays.fill(arr, Double.NaN);

        final int actualType = iter.currentSegment(arr);

        final String errMsg = String.format("Expected %d%s, was not: %d%s",
            Integer.valueOf(type), Arrays.toString(expected),
            Integer.valueOf(actualType), Arrays.toString(arr));

        assertEquals(errMsg, type, actualType);

        for (int i = 0; i < arr.length; ++i) {
            if (i < expected.length) {
                assertEquals(errMsg, expected[i], arr[i], DELTA_EPSILON);
            } else {
                assertEquals(errMsg, Double.NaN, arr[i], DELTA_IDENTITY);
            }
        }

        iter.next();
    }

    public static void assertDone(final PathIterator iter) {
        assertTrue(iter.isDone());
    }

    @Test
    public void testGetCoveredArea() {
        final RayGroup group = initialGroup();

        assertEquals(0, group.getReflectionLevel());
        assertEquals(0, group.getTravelledDistance(), DELTA_EPSILON);
        final RenderedBeam beam = group.toBeam().render(new AffineTransform());
        final ImmutablePoint s = DEFAULT_MODEL.getSource();
        assertIterator(beam.outline.getPathIterator(null),
            s, s, null, s, s);
        assertIterator(beam.area.getPathIterator(null),
            s, s, s);
    }

    private static RayGroup initialGroup() {
        return new SourceRayGroup(DEFAULT_MODEL);
    }
}
