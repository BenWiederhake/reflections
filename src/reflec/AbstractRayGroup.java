package reflec;

import java.util.Objects;

import com.google.common.base.Preconditions;

public abstract class AbstractRayGroup implements RayGroup {
    protected final Model model;

    private final int reflectionLevel;

    private final ImmutableLine gate;

    private final double travelledDistance;

    protected AbstractRayGroup(
    final Model model, final int reflectionLevel,
    final ImmutablePoint source, final ImmutableLine gate) {
        this.model = Objects.requireNonNull(model);
        this.reflectionLevel = reflectionLevel;
        this.gate = gate;
        travelledDistance = gate.ptSegDist(source);
        {
            final double gateLength = gate.p1.distance(gate.p2);
            if ((gate.p1 != source || gate.p2 != source)
                && gateLength < 0.000001) {
                System.err.format(
                    "WARNING: Gate of length %f (%s) found(%s)%n",
                    Double.valueOf(gateLength),
                    Double.toString(gateLength),
                    gate.toRawString());
            }
        }
        Preconditions.checkArgument(
            (gate.p1 == source && gate.p2 == source)
            ||                gate.relativeCCW(source) >= 0,
            "Gate %s is not ccw to RayGroup@%s",
            gate.toRawString(),
            source.toRawString());
    }

    @Override
    public final double getTravelledDistance() {
        return travelledDistance;
    }

    @Override
    public final int getReflectionLevel() {
        return reflectionLevel;
    }

    /**
     * Returns the gate. The fields p1 and p2 are ordered counter-clockwise.
     * Mnemonic: Counter Clockwise Counting
     * 
     * @return the gate, counting counter clockwise from the source
     */
    protected final ImmutableLine getGate() {
        return gate;
    }

    protected final String toIdentString() {
        return super.toString();
    }

    protected abstract Beam.Builder getCoveredAreaBuilder(
    final ImmutableLine via);

    public abstract Ray.Builder pathTo(final ImmutablePoint dst);
}
