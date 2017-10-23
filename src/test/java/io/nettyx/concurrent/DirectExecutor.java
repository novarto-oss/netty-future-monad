package io.nettyx.concurrent;

import java.util.concurrent.Executor;

public final class DirectExecutor implements Executor
{
    private DirectExecutor()
    {
    }

    public static final DirectExecutor INSTANCE = new DirectExecutor();

    @Override public void execute(Runnable command)
    {

        command.run();
    }
}
