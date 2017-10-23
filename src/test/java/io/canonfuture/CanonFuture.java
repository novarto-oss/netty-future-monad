package io.canonfuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CanonFuture<A>
{

    private CanonFuture()
    {
    }

    public static <A> CanonFuture<A> pure(A a)
    {
        return new Pure<>(a);
    }

    public static <A> CanonFuture<A> fork(Supplier<Executor> ex, Supplier<A> val)
    {
        return new Fork<>(ex, val);
    }

    public <B> CanonFuture<B> flatMap(Function<A, CanonFuture<B>> f)
    {
        return new FlatMap<>(this, f);
    }

    public <B> CanonFuture<B> map(Function<A, B> f)
    {
        return new FlatMap<>(this, x -> pure(f.apply(x)));
    }


    public abstract CompletableFuture<A> runStd();

    private static final class Pure<A> extends CanonFuture<A>
    {
        public final A pure;
        public Pure(A pure)
        {
            this.pure = pure;
        }

        @Override public CompletableFuture<A> runStd()
        {
            return CompletableFuture.completedFuture(pure);
        }
    }

    private static final class FlatMap<A, B> extends CanonFuture<B>
    {
        public final CanonFuture<A> upstream;
        public final Function<A, CanonFuture<B>> f;

        public FlatMap(CanonFuture<A> upstream, Function<A, CanonFuture<B>> f)
        {
            this.upstream = upstream;
            this.f = f;
        }

        @Override public CompletableFuture<B> runStd()
        {
            return upstream.runStd().thenCompose(x -> f.apply(x).runStd());
        }
    }

    private static final class Fork<A> extends CanonFuture<A>
    {

        public final Supplier<Executor> ex;
        public final Supplier<A> val;

        public Fork(Supplier<Executor> ex, Supplier<A> val)
        {
            this.ex = ex;
            this.val = val;
        }

        @Override public CompletableFuture<A> runStd()
        {
            return CompletableFuture.supplyAsync(val, ex.get());
        }
    }

}
