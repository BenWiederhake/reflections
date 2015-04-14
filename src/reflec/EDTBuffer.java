package reflec;

import java.awt.EventQueue;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;

public final class EDTBuffer<I, O> implements Buffer<I, O> {
    public static final Factory FACTORY = new Factory() {
        @Override
        public <I, O> Buffer<I, O> create(
        final Function<I, O> function, final OutputListener<O> listener,
        final I initialInput, final O nullOutput) {
            return new EDTBuffer<I, O>(function, listener,
                initialInput, nullOutput);
        }
    };

    private static final boolean VERBOSE = false;

    private final Function<I, O> function;

    private final OutputListener<O> listener;

    private final O nullOutput;

    private SwingWorker<O, Void> active;

    private O newestOut;

    private boolean hasNextInput;

    private I nextInput;

    private EDTBuffer(final Function<I, O> function,
    final OutputListener<O> listener,
    final I initialInput, final O nullOutput) {
        this.nullOutput = nullOutput;
        this.function = function;
        this.listener = listener;

        adjust(initialInput, true);
    }

    @Override
    public O get() {
        Preconditions.checkState(EventQueue.isDispatchThread());
        return newestOut;
    }

    @Override
    public void adjust(final I input, final boolean hard) {
        boolean update = false;
        Preconditions.checkState(EventQueue.isDispatchThread());
        if (hard) {
            /* Prepare for hard reset */
            if (null != active) {
                active.cancel(false);
                active = null;
            }
            newestOut = nullOutput;
            update = true;
        }
        if (null != active) {
            /*
             * Successfully avoided contention: Just leave the input there, it
             * will call process() again when the worker has completed.
             */
            hasNextInput = true;
            nextInput = input;
            if (VERBOSE) {
                System.out.println("EDTBuffer.adjust(): Swallowed");
            }
        } else {
            /*
             * Actually start a new worker.
             */
            hasNextInput = false;
            nextInput = null; // help gc
            if (VERBOSE) {
                System.out.println("EDTBuffer.adjust(): Start...");
            }
            active = new Worker(input);
            startWorker();
        }
        if (update) {
            /*
             * Only update now to make it impossible for the listener to call
             * adjust() in a bad moment.
             */
            listener.update(newestOut);
        }
    }

    protected void exception(final SwingWorker<O, Void> worker,
    final Exception e) {
        Preconditions.checkState(EventQueue.isDispatchThread());
        if (VERBOSE) {
            System.out.println("EDTBuffer.exception() FAILED");
        }
        if (worker != active) {
            /* Whoops, sorry. */
            return;
        }

        if (hasNextInput) {
            active = new Worker(nextInput);
            hasNextInput = false;
            nextInput = null; // help gc
            startWorker();
        } else {
            /*
             * Unrecoverable error. Dump stacktrace and revert to nullOutput, so
             * we don't "hang" but rather "switch the lights off"
             */
            e.printStackTrace();
            active = null;
            newestOut = nullOutput;
            listener.update(newestOut);
        }
    }

    private void startWorker() {
        /*
         * TODO: Use different thread pool to avoid TEN threads idling, when
         * there intentionally can only be one (or "very few") of them?
         */
        active.execute();
    }

    protected void completed(final SwingWorker<O, Void> worker,
    final O result) {
        Preconditions.checkState(EventQueue.isDispatchThread());
        if (worker != active) {
            if (VERBOSE) {
                System.out.println("EDTBuffer.completed(): Superfluous D:");
            }
            /* Whoops, sorry. */
            return;
        }

        if (hasNextInput) {
            active = new Worker(nextInput);
            startWorker();
            System.out.println("EDTBuffer.completed():"
                + " Intermediate complete, starting new ...");
        } else {
            active = null;
            if (VERBOSE) {
                System.out.println("EDTBuffer.completed(): Done.");
            }
        }
        hasNextInput = false;
        nextInput = null; // help gc
        newestOut = result;
        listener.update(result);
    }

    private final class Worker extends SwingWorker<O, Void>
    implements CancelStatus {
        private final I in;

        public Worker(final I in) {
            this.in = in;
        }

        @Override
        protected O doInBackground() throws Exception {
            if (VERBOSE) {
                System.out.println("EDTBuffer.Worker.doInBackground(): "
                    + Thread.currentThread());
            }
            return function.apply(in, this);
        }

        @Override
        protected void done() {
            Preconditions.checkState(EventQueue.isDispatchThread());
            O result;
            try {
                result = get();
            } catch (final InterruptedException e) {
                exception(this, e);
                return;
            } catch (final ExecutionException e) {
                exception(this, e);
                return;
            }
            completed(this, result);
        }
    }
}
