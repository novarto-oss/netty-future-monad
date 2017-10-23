package io.nettyx.concurrent;

import com.novarto.lang.ConcurrentUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.trane.future.CheckedFutureException;
import io.trane.future.FuturePool;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TraneFutureMonadThreadingSemanticsTest
{

    private static ExecutorService EX;

    @BeforeClass
    public static void setupEx()
    {
        EX = Executors.newCachedThreadPool(new DefaultThreadFactory("worker"));
    }

    @Test
    public void testIt() throws CheckedFutureException
    {
        FuturePool pool = FuturePool.apply(EX);
        // map is invoked in thread context of upstream
        assertThat(
                pool.async(() -> {
                    //if i uncomment this line, the test passes
                    //System.out.println("supplier: " + currentThreadName());

                    //if i uncomment the below block, the test passes
//                    try
//                    {
//                        Thread.sleep(20);
//                    }
//                    catch (InterruptedException e)
//                    {
//                        throw new RuntimeException(e);
//                    }

                    return 42;
                }).map(ignore -> {
                    System.out.println(currentThreadName());
                    return currentThreadName().contains("worker");
                })
                        .get(Duration.of(1, SECONDS)),
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
