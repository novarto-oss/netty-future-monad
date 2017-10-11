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
        return CompletableFutureMonad.unit(state.string);
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
        Future<Integer> result = FutureMonad.map(FutureMonad.unit(state.anInt), x -> x * 2);
        return result.get();
    }

    @Benchmark
    public Integer simpleMapGuava(BenchState state) throws ExecutionException, InterruptedException
    {
        ListenableFuture<Integer> result = FutureOpAliases.map(FutureOpAliases.unit(state.anInt), x -> x * 2);
        return result.get();
    }

    @Benchmark
    public Integer simpleMapStd(BenchState state) throws ExecutionException, InterruptedException
    {
        CompletableFuture<Integer> result = CompletableFutureMonad.map(CompletableFutureMonad.unit(state.anInt), x -> x * 2);
        return result.get();
    }

    @Benchmark
    public Integer mapChain(BenchState state) throws ExecutionException, InterruptedException
    {
        Future<Integer> result = FutureMonad.map(
                FutureMonad.map(
                        FutureMonad.map(
                                FutureMonad.map(FutureMonad.unit(state.anInt), x -> x * 2),
                                x -> x + 3
                        ),
                        x -> x - 20
                ),
                x -> x + 100
        );
        return result.get();
    }

    @Benchmark
    public Integer mapChainGuava(BenchState state) throws ExecutionException, InterruptedException
    {
        ListenableFuture<Integer> result = FutureOpAliases.map(
                FutureOpAliases.map(
                        FutureOpAliases.map(
                                FutureOpAliases.map(FutureOpAliases.unit(state.anInt), x -> x * 2),
                                x -> x + 3
                        ),
                        x -> x - 20
                ),
                x -> x + 100
        );
        return result.get();
    }

    @Benchmark
    public Integer mapChainStd(BenchState state) throws ExecutionException, InterruptedException
    {
        CompletableFuture<Integer> result = CompletableFutureMonad.map(
                CompletableFutureMonad.map(
                        CompletableFutureMonad.map(
                                CompletableFutureMonad.map(CompletableFutureMonad.unit(state.anInt), x -> x * 2),
                                x -> x + 3
                        ),
                        x -> x - 20
                ),
                x -> x + 100
        );
        return result.get();
    }


    @Benchmark
    public Integer flatMap(BenchState state) throws ExecutionException, InterruptedException
    {
        Future<Integer> result = FutureMonad.flatMap(
                FutureMonad.unit(state.anInt), x -> FutureMonad.unit(x * 2));
        return result.get();

    }

    @Benchmark
    public Integer flatMapGuava(BenchState state) throws ExecutionException, InterruptedException
    {
        ListenableFuture<Integer> result = FutureOpAliases.bind(
                FutureOpAliases.unit(state.anInt), x -> FutureOpAliases.unit(x * 2));
        return result.get();

    }

    @Benchmark
    public Integer flatMapStd(BenchState state) throws ExecutionException, InterruptedException
    {
        CompletableFuture<Integer> result = CompletableFutureMonad.flatMap(
                CompletableFutureMonad.unit(state.anInt), x -> CompletableFutureMonad.unit(x * 2));
        return result.get();

    }

    @Benchmark
    public Integer flatMapChain(BenchState state) throws ExecutionException, InterruptedException
    {
        Future<Integer> result = FutureMonad.flatMap(
                FutureMonad.flatMap(
                        FutureMonad.flatMap(
                                FutureMonad.map(FutureMonad.unit(state.anInt), x -> x * 2),
                                x -> FutureMonad.unit(x - 10)),
                        x -> FutureMonad.unit(x + 13)),
                x -> FutureMonad.unit(x / 2));
        return result.get();
    }

    @Benchmark
    public Integer flatMapChainGuava(BenchState state) throws ExecutionException, InterruptedException
    {
        ListenableFuture<Integer> result = FutureOpAliases.bind(
                FutureOpAliases.bind(FutureOpAliases.bind(
                        FutureOpAliases.map(FutureOpAliases.unit(state.anInt), x -> x * 2),
                        x -> FutureOpAliases.unit(x - 10)),
                        x -> FutureOpAliases.unit(x + 13)),
                x -> FutureOpAliases.unit(x / 2));
        return result.get();
    }

    @Benchmark
    public Integer flatMapChainStd(BenchState state) throws ExecutionException, InterruptedException
    {
        CompletableFuture<Integer> result = CompletableFutureMonad.flatMap(
                CompletableFutureMonad.flatMap(
                        CompletableFutureMonad.flatMap(
                                CompletableFutureMonad.map(CompletableFutureMonad.unit(state.anInt), x -> x * 2),
                                x -> CompletableFutureMonad.unit(x - 10)),
                        x -> CompletableFutureMonad.unit(x + 13)),
                x -> CompletableFutureMonad.unit(x / 2));
        return result.get();
    }

}

