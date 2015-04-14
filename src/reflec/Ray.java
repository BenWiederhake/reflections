package reflec;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Ray {
    public final ImmutableList<ImmutablePoint> points;

    private Ray(final ImmutableList<ImmutablePoint> points) {
        this.points = points;
    }

    public boolean equals(final Object obj) {
        return (obj instanceof Ray)
            && points.equals(((Ray) obj).points);
    }

    public int hashCode() {
        return points.hashCode() * 37 - 9681354;
    }

    public static Builder builder(final ImmutablePoint source,
    final ImmutablePoint via) {
        return new Builder(source, via);
    }

    public static final class Builder {
        private ImmutableList.Builder<ImmutablePoint> points;

        private boolean built;

        public Builder(final ImmutablePoint source,
        final ImmutablePoint via) {
            points = ImmutableList.builder();
            points.add(source);
            points.add(via);
        }

        public void add(final ImmutablePoint p) {
            Preconditions.checkState(!built);
            points.add(p);
        }

        public Ray build() {
            Preconditions.checkState(!built);
            built = true;
            return new Ray(points.build());
        }
    }
}
