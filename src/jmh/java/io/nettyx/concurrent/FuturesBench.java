package io.nettyx.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import com.novarto.lang.guava.FutureOpAliases;
import io.netty.util.concurrent.Future;
import org.openjdk.jmh.annotations.Benchmark;

import java.util.concurrent.CompletableFuture;

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
}
