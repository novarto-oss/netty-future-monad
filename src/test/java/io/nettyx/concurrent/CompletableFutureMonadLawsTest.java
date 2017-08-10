package io.nettyx.concurrent;

import fj.test.Arbitrary;
import fj.test.Cogen;
import fj.test.Gen;
import fj.test.Property;
import fj.test.runner.PropertyTestRunner;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static fj.test.Arbitrary.arbBoolean;
import static fj.test.Arbitrary.arbInteger;
import static fj.test.Cogen.cogenInteger;
import static fj.test.Property.prop;
import static fj.test.Property.property;
import static io.nettyx.concurrent.CompletableFutureMonad.*;

@RunWith(PropertyTestRunner.class)
public class CompletableFutureMonadLawsTest
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


    private static <A, B> Gen<Function<A, CompletableFuture<B>>> arbSuccessfulAsyncFuntion(Cogen<A> aGen, Gen<B> bGen)
    {
        return Arbitrary.arbF(aGen, bGen).map(f -> x -> unit(f.f(x)));
    }

    private static <A> Gen<CompletableFuture<A>> arbSuccessfulFuture(Gen<A> aGen)
    {
        return aGen.map(x -> unit(x));
    }


    private static <A> Gen<CompletableFuture<A>> arbFailedFuture()
    {
        return Arbitrary.arbThrowable(Arbitrary.arbString).map(x -> failed(x));
    }

    private static <A> Gen<CompletableFuture<A>> arbFuture(Gen<A> aGen)
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

    private static <A, B> Gen<Function<A, CompletableFuture<B>>> arbFailedAsyncFunction()
    {
        return CompletableFutureMonadLawsTest.<B>arbFailedFuture().map(fut -> x -> fut);
    }

    private static <A, B> Gen<Function<A, CompletableFuture<B>>> arbAsyncFunction(Cogen<A> aGen, Gen<B> bGen)
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

    private static <A> boolean futuresEqual(CompletableFuture<A> x, CompletableFuture<A> y)
    {
        try
        {
            A xRes = x.get();
            try
            {
                A yRes = y.get();
                return xRes.equals(yRes);
            }
            catch (Exception yEx)
            {
                return false;
            }
        }
        catch (Throwable xEx)
        {
            try
            {
                y.get();
                return false;
            }
            catch (Throwable yEx)
            {
                return xEx.equals(yEx) || xEx.getCause().equals(yEx.getCause());
            }
        }




    }



}
