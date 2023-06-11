package org.team5183.beeapi.runnables;

import org.jetbrains.annotations.NotNull;

public abstract class EndableRunnable implements NamedRunnable {
    @NotNull RunnableStatus status = RunnableStatus.RUNNING;

    public abstract void shutdown();

    public @NotNull RunnableStatus getStatus() {
        return status;
    }
}
