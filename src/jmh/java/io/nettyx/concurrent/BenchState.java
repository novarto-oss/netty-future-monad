package io.nettyx.concurrent;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.function.Function;

@State(Scope.Benchmark)
public class BenchState
{
    public String string = "hello olleh";
    public Integer anInt = 42;

    public Function<Integer, Integer> doubleItFn = x -> x * 2;

    public Integer doubleIt(Integer in)
    {
        return in * 2;
    }
}
