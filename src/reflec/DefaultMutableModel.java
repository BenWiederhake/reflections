package reflec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import reflec.RayGroup.ReflectionResult;

import com.google.common.collect.ImmutableList;

public final class DefaultMutableModel implements MutableModel {
    private static final int DEFAULT_DEPTH = -1;

    private final List<ModelListener> panels = new LinkedList<>();

    private Model model;

    private ImmutableList<RayGroup> groups;

    private ImmutableList<Ray> rays;

    private int depth;

    public DefaultMutableModel(final Model model) {
        this(model, DEFAULT_DEPTH);
    }

    public DefaultMutableModel(final Model model, final int depth) {
        this.depth = depth;
        this.model = model;
        doUpdate();
    }

    @Override
    public Model getModel() {
        return model;
    }

    private void doUpdate() {
        this.model = checkNotNull(model);

        try {
            final ImmutableList.Builder<Ray> raysBuilder =
                ImmutableList.builder();

            final Deque<RayGroup> pending = new LinkedList<>();
            pending.add(new SourceRayGroup(model));

            while (!pending.isEmpty()
                && pending.getFirst().getReflectionLevel() <= depth) {
                final RayGroup g = pending.remove();

                final ReflectionResult result = g.doReflection();

                final Ray ray = result.getFoundRay();
                if (null != ray) {
                    raysBuilder.add(ray);
                }
                pending.addAll(result.getGroups());
            }

            groups = ImmutableList.copyOf(pending);
            rays = raysBuilder.build();

            fireModelChanged();
        } catch (final RuntimeException e) {
            System.err.println("Couldn't update: " + e.toString());
        }
    }

    public void setModel(final Model model) {
        if (model != this.model) {
            this.model = checkNotNull(model);
            doUpdate();
        }
    }

    public void setDepth(final int depth) {
        if (depth != this.depth) {
            checkArgument(depth >= -1);
            this.depth = depth;
            doUpdate();
        }
    }

    public int getDepth() {
        return depth;
    }

    // public void set(final Model model, final ImmutableList<RayGroup> groups,
    // final ImmutableList<Ray> rays) {
    // this.model = checkNotNull(model);
    // this.groups = checkNotNull(groups);
    // this.rays = checkNotNull(rays);
    // fireModelChanged();
    // }
    //
    // public void update(final Model model, final ImmutableList<RayGroup>
    // groups,
    // final ImmutableList<Ray> rays) {
    // checkArgument(null != model || null != groups || null != rays);
    // this.model = Objects.firstNonNull(model, this.model);
    // this.groups = Objects.firstNonNull(groups, this.groups);
    // this.rays = Objects.firstNonNull(rays, this.rays);
    // fireModelChanged();
    // }

    @Override
    public ImmutableList<RayGroup> getGroups() {
        return groups;
    }

    @Override
    public ImmutableList<Ray> getRays() {
        return rays;
    }

    public boolean add(final ModelListener e) {
        return panels.add(checkNotNull(e));
    }

    private void fireModelChanged() {
        for (final ModelListener panel : panels) {
            panel.notifyModelChanged(false);
        }
    }
}
