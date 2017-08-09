package io.nettyx.concurrent;

import fj.test.Arbitrary;
import fj.test.Cogen;
import fj.test.Gen;
import fj.test.Property;
import fj.test.runner.PropertyTestRunner;
import io.netty.util.concurrent.Future;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.function.Function;

import static fj.test.Arbitrary.arbBoolean;
import static fj.test.Arbitrary.arbInteger;
import static fj.test.Cogen.cogenInteger;
import static fj.test.Property.prop;
import static fj.test.Property.property;
import static io.nettyx.concurrent.FutureMonad.*;
import static io.nettyx.concurrent.FutureUtil.ready;
import static java.time.temporal.ChronoUnit.SECONDS;

@RunWith(PropertyTestRunner.class)
public class FutureMonadLawsTest
{

    /**
     * forall x: A, f: A => Future[B] { flatMap(unit(x),f) === f(x) }
     */
    public Property leftIdentity()
    {
        return property(arbInteger, arbAsyncFunction(cogenInteger, arbInteger), (x, f) -> prop(
                futuresEqual(flatMap(unit(x), f), f.apply(x))
        ));
    }

    /**
     * forall future: Future[A]  {flatMap(future, x -> unit(x)) === future}
     */
    public Property rightIdentity()
    {
        return property(arbFuture(arbInteger), future -> prop(
                futuresEqual(flatMap(future, x -> unit(x)), future)
        ));
    }

    /**
     * forall future: Future[A], f: A => Future[B], g: A => Future[B]
     *      { flatMap(flatMap(future, f), g) === flatMap(future, x -> flatMap(f(x), g)}
     */
    public Property associativity()
    {
        return property(
                arbFuture(arbInteger),
                arbAsyncFunction(cogenInteger, arbInteger),
                arbAsyncFunction(cogenInteger, arbInteger),

                (future, f, g) -> prop(futuresEqual(
                        flatMap(flatMap(future, f), g),
                        flatMap(future, x -> flatMap(f.apply(x), g))
                ))
        );
    }


    private static <A> boolean successfulFutureEqual(Future<A> x, Future<A> y)
    {
        A xNow = x.getNow();
        A yNow = y.getNow();

        if (xNow == null || yNow == null)
        {
            return false;
        }

        return xNow.equals(yNow);

    }

    private static <A, B> Gen<Function<A, Future<B>>> arbSuccessfulAsyncFuntion(Cogen<A> aGen, Gen<B> bGen)
    {
        return Arbitrary.arbF(aGen, bGen).map(f -> x -> unit(f.f(x)));
    }

    private static <A> Gen<Future<A>> arbSuccessfulFuture(Gen<A> aGen)
    {
        return aGen.map(x -> unit(x));
    }


    private static <A> Gen<Future<A>> arbFailedFuture()
    {
        return Arbitrary.arbThrowable(Arbitrary.arbString).map(x -> failed(x));
    }

    private static <A> Gen<Future<A>> arbFuture(Gen<A> aGen)
    {
        return arbBoolean.bind(success -> {
            if (success)
            {
                return arbSuccessfulFuture(aGen);
            }
            else
            {
                return arbFailedFuture();
            }
        });
    }

    private static <A, B> Gen<Function<A, Future<B>>> arbFailedAsyncFunction()
    {
        return FutureMonadLawsTest.<B>arbFailedFuture().map(fut -> x -> fut);
    }

    private static <A, B> Gen<Function<A, Future<B>>> arbAsyncFunction(Cogen<A> aGen, Gen<B> bGen)
    {
        return arbBoolean.bind(success -> {
            if (success)
            {
                return arbSuccessfulAsyncFuntion(aGen, bGen);
            }
            else
            {
                return arbFailedAsyncFunction();

            }
        });
    }

    private static <A> boolean futuresEqual(Future<A> x, Future<A> y)
    {
        ready(x, Duration.of(1, SECONDS));
        ready(y, Duration.of(1, SECONDS));

        if (x.cause() != null)
        {
            return y.cause() != null && x.cause().equals(y.cause());
        }

        else
        {
            return successfulFutureEqual(x, y);
        }

    }


}
