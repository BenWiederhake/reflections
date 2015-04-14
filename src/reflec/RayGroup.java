package reflec;

import java.util.Objects;

import com.google.common.collect.ImmutableList;

public interface RayGroup {
    double getTravelledDistance();

    int getReflectionLevel();

    ReflectionResult doReflection();

    Beam toBeam();

    public static final class ReflectionResult {
        public static final ReflectionResult EMPTY =
            new ReflectionResult(ImmutableList.<RayGroup> of(), null);

        private final ImmutableList<RayGroup> groups;

        private final Ray foundRay;

        public ReflectionResult(final ImmutableList<RayGroup> groups,
        final Ray foundRay) {
            this.groups = Objects.requireNonNull(groups);
            this.foundRay = foundRay;
        }

        public ImmutableList<RayGroup> getGroups() {
            return groups;
        }

        public Ray getFoundRay() {
            return foundRay;
        }
    }
}
