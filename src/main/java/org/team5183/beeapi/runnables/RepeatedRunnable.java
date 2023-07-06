package org.team5183.beeapi.runnables;

import org.jetbrains.annotations.NotNull;

public abstract class RepeatedRunnable extends NamedRunnable {
    public abstract void shutdown();

    @Override
    public @NotNull RunnableType getType() {
        return RunnableType.REPEATED;
    }
}
