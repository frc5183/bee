package org.team5183.beeapi.runnables;

import org.jetbrains.annotations.NotNull;

public abstract class OneshotRunnable extends NamedRunnable {
    @Override
    public @NotNull RunnableType getType() {
        return RunnableType.ONESHOT;
    }
}