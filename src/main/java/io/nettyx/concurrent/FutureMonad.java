package io.nettyx.concurrent;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.nettyx.concurrent.FutureUtil.failOrEffect;

public class FutureMonad
{
    public static <A, B> Future<B> map(Future<A> fut, Function<A, B> f)
    {
        Promise<B> result = ImmediateEventExecutor.INSTANCE.newPromise();
        failOrEffect(fut, result, () -> result.setSuccess(f.apply(fut.getNow())));
        return result;
    }


    public static <A, B> Future<B> flatMap(Future<A> fut, Function<A, Future<B>> f)
    {
        Promise<B> promise = ImmediateEventExecutor.INSTANCE.newPromise();
        failOrEffect(fut, promise, () -> {
            Future<B> fut2 = f.apply(fut.getNow());
            failOrEffect(fut2, promise, () -> promise.setSuccess(fut2.getNow()));
        });

        return promise;
    }

    public static <A> Future<A> unit(A a)
    {
        return ImmediateEventExecutor.INSTANCE.newSucceededFuture(a);
    }

    public static <A> Future<A> flatten(Future<Future<A>> fut)
    {
        return flatMap(fut, x -> x);

    }

    public static <A, B> Future<B> fold(Iterable<Future<A>> xs, BiFunction<B, A, B> f, B zero)
    {
        Future<B> acc = unit(zero);
        for (Future<A> future : xs)
        {
            acc = flatMap(acc, x -> map(future, y -> f.apply(x, y)));
        }
        return acc;
    }

    public static <A> Future<A> failed(Throwable cause)
    {
        return ImmediateEventExecutor.INSTANCE.newFailedFuture(cause);
    }

    public static <A> Future<A> fork(Future<A> fut, EventExecutor ex)
    {
        Promise<A> result = ex.newPromise();
        failOrEffect(fut, result, () -> result.setSuccess(fut.getNow()));
        return result;
    }

    public static <A> Future<A> fork(A a, EventExecutor ex)
    {
        return ex.newSucceededFuture(a);
    }

    public static <A> Future<A> fork(Supplier<A> f, EventExecutor ex)
    {
        return ex.submit(() -> f.get());
    }


}
