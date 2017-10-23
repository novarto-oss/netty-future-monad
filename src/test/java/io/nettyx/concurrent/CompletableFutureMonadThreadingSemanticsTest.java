package io.nettyx.concurrent;

import com.novarto.lang.ConcurrentUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.function.Function;

import static io.nettyx.concurrent.CompletableFutureMonad.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CompletableFutureMonadThreadingSemanticsTest
{

    private static ExecutorService EX;

    @BeforeClass
    public static void setupEx()
    {
        EX = Executors.newCachedThreadPool(new DefaultThreadFactory("worker"));
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

        Function<Integer, Boolean> f = ignore -> currentThreadName().contains("worker");

        CompletableFuture<Integer> fut = fork(() -> 42, EX);

        assertThat(
                map(fut, f)
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
//        assertThat(
//                flatMap(fork(unit(42), EX), ignore -> unit(currentThreadName().contains("worker")))
//                        .get(),
//                is(true)
//        );


        // forking with a supplier executes the supplier in the supplied context
        assertThat(
                fork(() -> currentThreadName().contains("worker"), EX)
                        .get(),
                is(true)
        );

        // forking a pure value changes the context to the supplied one
        CompletableFuture<Integer> in = fork(42, EX);
        assertThat(
                map(in, ignore -> {
                            System.out.println("zzz " + currentThreadName());
                            return currentThreadName().contains("worker");
                        })
                        .get(),
                is(true)
        );

        // forking a pure value changes the context to the supplied one
        assertThat(
                map(fork(unit(42), EX), ignore -> currentThreadName().contains("worker"))
                        .get(),
                is(true)
        );

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
                        flatMap(unit(42), x -> fork(() -> x*2, EX)),
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
