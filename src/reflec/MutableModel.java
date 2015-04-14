package reflec;

import com.google.common.collect.ImmutableList;

public interface MutableModel {
    Model getModel();

    ImmutableList<RayGroup> getGroups();

    ImmutableList<Ray> getRays();
    
    public interface ModelListener {
        void notifyModelChanged(final boolean coreModelChanged);
    }
}
