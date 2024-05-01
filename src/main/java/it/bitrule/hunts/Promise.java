package it.bitrule.hunts;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class Promise<T> {

    private static final @NonNull ExecutorService ASYNC_EXECUTOR = new ThreadPoolExecutor(
            10,
            Integer.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new BasicThreadFactory.Builder()
                    .namingPattern("Stray Executor")
                    .daemon(true)
                    .build()
    );

    private static boolean executorServiceShutdown = false;

    private final @NonNull CompletableFuture<T> backingFuture;

    @Getter private boolean done = false;

    public Promise() {
        this.backingFuture = new CompletableFuture<>();
    }

    /**
     * Create a new async promise
     * @param supplier async task
     * @param <V>      ObjectType involved in this promise
     * @return a new Promise
     */
    public static @NonNull <V> Promise<V> supplyAsync(@NonNull Supplier<V> supplier) {
        if (executorServiceShutdown) {
            throw new IllegalStateException("Executor service is shutdown");
        }

        if (isPrimaryThread()) {
            Promise<V> promise = new Promise<>();
            ASYNC_EXECUTOR.execute(() -> {
                try {
                    promise.complete(supplier.get());
                } catch (Exception e) {
                    promise.completeExceptionally(e);
                }
            });

            return promise;
        }

        try {
            return completedFuture(supplier.get());
        } catch (Exception e) {
            return failedFuture(e);
        }
    }

    /**
     * Create a new async promise
     * @param runnable async task
     */
    public static void runAsync(@NonNull Runnable runnable) {
        if (executorServiceShutdown) {
            throw new IllegalStateException("Executor service is shutdown");
        }

        if (isPrimaryThread()) {
            ASYNC_EXECUTOR.execute(runnable);
        } else {
            runnable.run();
        }
    }

    public static void shutdown() {
        executorServiceShutdown = true;

        try {
            ASYNC_EXECUTOR.shutdown();

            if (!ASYNC_EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) return;

            ASYNC_EXECUTOR.shutdownNow();

            if (ASYNC_EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
                ASYNC_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            ASYNC_EXECUTOR.shutdownNow();

            throw new RuntimeException(e);
        }
    }

    public static boolean isPrimaryThread() {
        return !Thread.currentThread().getName().contains("Stray Executor");
    }

    public void complete(@Nullable T value) {
        this.backingFuture.complete(value);

        this.done = true;
    }

    public void completeExceptionally(@NonNull Throwable throwable) {
        this.backingFuture.completeExceptionally(throwable);
    }

    public @NonNull T now() {
        return this.backingFuture.join();
    }

    public void whenComplete(@NonNull BiConsumer<T, Throwable> biConsumer) {
        this.backingFuture.whenComplete((value, throwable) -> {
            try {
                biConsumer.accept(value, throwable);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        });
    }

    public static <V> @NonNull Promise<V> completedFuture(@NonNull V value) {
        Promise<V> promise = new Promise<>();
        promise.complete(value);

        return promise;
    }

    public static <V> @NonNull Promise<V> failedFuture(@NonNull Throwable value) {
        Promise<V> promise = new Promise<>();
        promise.completeExceptionally(value);

        return promise;
    }
}