package io.nettyx.concurrent;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class BenchState
{
    public String string = "hello olleh";
}
