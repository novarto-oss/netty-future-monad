package io.nettyx.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import com.novarto.lang.guava.FutureOpAliases;
import io.netty.util.concurrent.Future;
import org.openjdk.jmh.annotations.Benchmark;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FuturesBench
{

    @Benchmark
    public String baselineReturnConst()
    {
        return "alabala";
    }

    @Benchmark
    public Object baselineCreateObject()
    {
        return new Object();
    }

    @Benchmark
    public Future<String> unit(BenchState state)
    {
        return FutureMonad.unit(state.string);
    }

    @Benchmark
    public ListenableFuture<String> guavaUnit(BenchState state)
    {
        return FutureOpAliases.unit(state.string);
    }

    @Benchmark
    public CompletableFuture<String> stdUnit(BenchState state)
    {
        CompletableFuture<String> result = new CompletableFuture<>();
        result.complete(state.string);
        return result;
    }

    @Benchmark
    public Integer simpleMapConst(BenchState state) throws ExecutionException, InterruptedException
    {
        Future<Integer> result = FutureMonad.map(FutureMonad.unit(state.anInt), state.doubleItFn);
        return result.get();
    }

    @Benchmark
    public Integer simpleMapMethodHandle(BenchState state) throws ExecutionException, InterruptedException
    {
        Future<Integer> result = FutureMonad.map(FutureMonad.unit(state.anInt), state::doubleIt);
        return result.get();
    }

    @Benchmark
    public Integer simpleMap(BenchState state) throws ExecutionException, InterruptedException
    {
        Future<Integer> result = FutureMonad.map(FutureMonad.unit(state.anInt), x -> x*2);
        return result.get();
    }

    @Benchmark
    public Integer simpleMapGuava(BenchState state) throws ExecutionException, InterruptedException
    {
        ListenableFuture<Integer> result = FutureOpAliases.map(FutureOpAliases.unit(state.anInt), x -> x*2);
        return result.get();
    }

    @Benchmark
    public Integer simpleMapStd(BenchState state) throws ExecutionException, InterruptedException
    {
        CompletableFuture<Integer> fut = new CompletableFuture<>();
        fut.complete(state.anInt);
        CompletableFuture<Integer> result = fut.thenApply(x -> x * 2);
        return result.get();
    }
}

