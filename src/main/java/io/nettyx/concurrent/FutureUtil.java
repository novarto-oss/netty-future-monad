package io.nettyx.concurrent;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class FutureUtil
{

    public static void failOrEffect(Future<?> upstream, Promise<?> promise, Effect0 f)
    {
        upstream.addListener(x ->
        {
            if (upstream.isCancelled())
            {
                promise.cancel(false);
            }
            else if (upstream.cause() != null)
            {
                promise.setFailure(x.cause());
            }
            else
            {
                f.f();
            }
        });
    }

    public static <A> void addCallback(Future<A> future, Consumer<A> onSuccess, Consumer<Throwable> onFailure)
    {
        future.addListener(x -> {
            Throwable cause = x.cause();
            if (x.isCancelled() && cause == null)
            {
                throw new IllegalStateException();
            }

            if (cause != null)
            {
               onFailure.accept(cause);
            }
            else
            {
                onSuccess.accept(future.getNow());
            }

        });
    }

    /**
     * Awaits the completion of future with timeout duration.
     * Method will always return Completed future.
     * If computation of the passed future is not finished within specified timeout, a completed failed
     * future with {@link TimeoutException} will be returned.
     *
     * @param future
     * @param duration
     * @return Completed future.
     */
    public static <A> Future<A> ready(final Future<A> future, final Duration duration)
    {
        final Promise<A> result = ImmediateEventExecutor.INSTANCE.newPromise();

        addCallback(future, x -> result.setSuccess(x), t -> result.setFailure(t));

        final long start = System.nanoTime();

        long currStepNanos = Math.max(1000000, duration.toNanos() / 100000000000L);

        while (!result.isDone())
        {
            final long elapsed = System.nanoTime() - start;
            final long remaining = duration.toNanos() - elapsed;

            if (remaining <= 0)
            {
                result.setFailure(new TimeoutException("Failed to complete for " + duration.toNanos()));
                break;
            }

            LockSupport.parkNanos(Math.min(remaining, currStepNanos));

            currStepNanos = currStepNanos << 1;

        }

        return result;

    }


}
