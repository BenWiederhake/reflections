package reflec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.google.common.collect.ImmutableSet;

public final class Model {
    public static final String FILE_NAME = "sample.txt";

    private final ImmutableSet<ImmutableLine> mirrors;

    private final ImmutablePoint source;

    private final ImmutablePoint sink;

    public Model(final ImmutableSet<ImmutableLine> mirrors,
    final ImmutablePoint source, final ImmutablePoint sink) {
        this.mirrors = mirrors;
        this.source = source;
        this.sink = sink;
    }

    public ImmutablePoint getSource() {
        return source;
    }

    public ImmutablePoint getSink() {
        return sink;
    }

    public ImmutableSet<ImmutableLine> getMirrors() {
        return mirrors;
    }

    @Override
    public String toString() {
        return String.format("Model[%s->%s @ %d mirrors]",
            source, sink, Integer.valueOf(mirrors.size()));
    }

    public static Model from(final Scanner scanner) throws IOException {
        final Model ret;
        final Locale initialLocale = scanner.locale();
        scanner.useLocale(Locale.US);
        try {
            final Builder b = builder(parsePoint(scanner), parsePoint(scanner));
            while (scanner.hasNext()) {
                b.add(new ImmutableLine(
                    parsePoint(scanner),
                    parsePoint(scanner)));
            }
            ret = b.build();
        } finally {
            scanner.useLocale(initialLocale);
        }

        return ret;
    }

    private static ImmutablePoint parsePoint(final Scanner scanner)
    throws IOException {
        try {
            return new ImmutablePoint(
                scanner.nextDouble(),
                scanner.nextDouble());
        } catch (InputMismatchException e) {
            throw new IOException(e);
        } catch (NoSuchElementException e) {
            throw new IOException(e);
        } catch (IllegalStateException e) {
            throw new IOException(e);
        }
    }

    public static void main(final String[] args) {
        System.out.println(defaultModel());
    }

    public static Model defaultModel() {
        try {
            return Model.from(new Scanner(new File(FILE_NAME)));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Builder builder(
    final ImmutablePoint source, final ImmutablePoint sink) {
        return new Builder(source, sink);
    }

    public static final class Builder {
        private final ImmutablePoint source;

        private final ImmutablePoint sink;

        private final ImmutableSet.Builder<ImmutableLine> mirrors;

        public Builder(final ImmutablePoint source, final ImmutablePoint sink) {
            this.source = source;
            this.sink = sink;
            this.mirrors = ImmutableSet.builder();
        }

        public void add(final ImmutableLine line) {
            mirrors.add(line);
        }

        public Model build() {
            return new Model(mirrors.build(), source, sink);
        }
    }
}
