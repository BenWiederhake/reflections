package reflec;

public final class PhonyBuffer<I, O> implements Buffer<I, O> {
    public static final Factory FACTORY = new Factory() {
        @Override
        public <I, O> Buffer<I, O> create(
        final Function<I, O> function, final OutputListener<O> listener,
        final I initialInput, final O nullOutput) {
            return new PhonyBuffer<I, O>(function, listener, initialInput);
        }
    };
    
    private static final CancelStatus CONTINUE_STATUS = new CancelStatus() {
        @Override
        public boolean isCancelled() {
            return false;
        }
    };

    private final Function<I, O> function;

    private final OutputListener<O> listener;

    private I lastInput;

    private O lastOutput;

    public PhonyBuffer(
    final Function<I, O> function, final OutputListener<O> listener,
    final I initialInput) {
        this.function = function;
        this.listener = listener;
        this.lastInput = initialInput;
        this.lastOutput = function.apply(initialInput, CONTINUE_STATUS);
        listener.update(lastOutput);
    }

    @Override
    public O get() {
        return lastOutput;
    }

    @Override
    public void adjust(final I input, final boolean hard) {
        if (input != lastInput) {
            lastInput = input;
            lastOutput = function.apply(input, CONTINUE_STATUS);
            listener.update(lastOutput);
        }
    }
}
