package reflec;

public interface Buffer<I, O> {
    O get();

    void adjust(I input, boolean hard);

    public interface Factory {
        <I, O> Buffer<I, O> create(
        final Function<I, O> function, final OutputListener<O> listener,
        final I initialInput, final O nullOutput);
    }

    public interface CancelStatus {
        boolean isCancelled();
    }

    public interface Function<I, O> {
        O apply(I in, CancelStatus status);
    }

    public interface OutputListener<O> {
        void update(O result);
    }
}
