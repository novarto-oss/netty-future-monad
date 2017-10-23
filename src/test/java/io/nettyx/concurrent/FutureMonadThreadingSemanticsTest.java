package io.nettyx.concurrent;

import com.novarto.lang.ConcurrentUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.nettyx.concurrent.FutureMonad.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FutureMonadThreadingSemanticsTest
{

    private static EventExecutor EX;

    @BeforeClass
    public static void setupEx()
    {
        EX = new UnorderedThreadPoolEventExecutor(20, new DefaultThreadFactory("worker"));
    }

    @Test public void testIt() throws ExecutionException, InterruptedException
    {
        String main = currentThreadName();

        // map is invoked in thread context of upstream
        assertThat(
                map(unit(42), ignore -> currentThreadName().equals(main))
                        .get(),
                is(true)
        );

        // map is invoked in thread context of upstream
        assertThat(
                map(fork(unit(42), EX), ignore -> currentThreadName().contains("worker"))
                        .get(),
                is(true)
        );

        // flatMap is invoked in thread context of upstream
        assertThat(
                flatMap(unit(42), ignore -> unit(currentThreadName().equals(main)))
                        .get(),
                is(true)
        );

        // flatMap is invoked in thread context of upstream
        assertThat(
                flatMap(fork(unit(42), EX), ignore -> unit(currentThreadName().contains("worker")))
                        .get(),
                is(true)
        );


        // forking with a supplier executes the supplier in the supplied context
        assertThat(
                fork(() -> currentThreadName().contains("worker"), EX)
                        .get(),
                is(true)
        );

        // forking a pure value changes the context to the supplied one
        Future<Integer> in = fork(42, EX);
        assertThat(
                map(in, ignore -> currentThreadName().contains("worker"))
                        .get(),
                is(true)
        );

        // forking a pure value changes the context to the supplied one
        assertThat(
                map(fork(unit(42), EX), ignore -> currentThreadName().contains("worker"))
                        .get(),
                is(true)
        );

        Future<Integer> forked = flatMap(unit(42), x -> EX.submit(() -> {
            System.out.println("in supplier: " + currentThreadName());
            return x * 2;
        }));

        Future<Integer> forkMapped = map(forked, x -> {
            System.out.println("in map of forked: " + currentThreadName());
            return x;
        });


        forkMapped.get();

        // when flatMap changes the context, subsequent operations are in that context
        assertThat(
                map(
                        flatMap(unit(42), x -> fork(x, EX)),
                        ignore -> {
                            System.out.println(currentThreadName());
                            return currentThreadName().contains("worker");
                        }
                ).get(),
                is(true)
        );

        // when flatMap changes the context, subsequent operations are in that context
        assertThat(
                map(
                        flatMap(unit(42), x -> EX.submit(() -> x*2)),
                        ignore -> {
                            System.out.println(currentThreadName());
                            return currentThreadName().contains("worker");
                        }
                ).get(),
                is(true)
        );


    }

    private static String currentThreadName()
    {
        return Thread.currentThread().getName();
    }

    @AfterClass
    public static void teardownEx()
    {
        ConcurrentUtil.shutdownAndAwaitTermination(EX, 5, TimeUnit.SECONDS);

    }



}
