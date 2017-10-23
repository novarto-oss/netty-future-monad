package io.nettyx.concurrent;

import fj.Unit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureMonad
{
    public static <A> CompletableFuture<A> unit(A value)
    {
        CompletableFuture<A> fut = new CompletableFuture<>();
        fut.complete(value);
        return fut;
    }

    public static <A, B> CompletableFuture<B> map(CompletableFuture<A> fut, Function<A, B> f)
    {
        CompletableFuture<Unit> trigger = new CompletableFuture<>();

        CompletableFuture<A> withTrigger = fut.thenCombineAsync(trigger, (x, ignore) -> x, DirectExecutor.INSTANCE);
        CompletableFuture<B> result = withTrigger.thenApply(f);

        trigger.complete(Unit.unit());

        return result;

    }

    public static <A, B> CompletableFuture<B> flatMap(CompletableFuture<A> fut,
            Function<? super A, ? extends CompletionStage<B>> f)
    {
        CompletableFuture<Unit> trigger = new CompletableFuture<>();

        CompletableFuture<A> withTrigger = fut.thenCombineAsync(trigger, (x, ignore) -> x, DirectExecutor.INSTANCE);

        CompletableFuture<B> result = withTrigger.thenCompose(f);

        trigger.complete(Unit.unit());

        return result;
    }

    public static <A> CompletableFuture<A> failed(Throwable ex)
    {
        CompletableFuture<A> fut = new CompletableFuture<>();
        fut.completeExceptionally(ex);
        return fut;
    }

    public static <A> CompletableFuture<A> fork(A pure, Executor ex)
    {
        return fork(() -> pure, ex);
    }

    public static <A> CompletableFuture<A> fork(Supplier<A> supplier, Executor ex)
    {
        return CompletableFuture.supplyAsync(supplier, ex);
    }

    public static <A> CompletableFuture<A> fork(CompletableFuture<A> fut, Executor ex)
    {
        return flatMap(fut, x -> fork(x, ex));
    }
}
