package org.team5183.beeapi.runnables;

import org.jetbrains.annotations.NotNull;

public abstract class NamedRunnable implements Runnable {
    RunnableStatus status = RunnableStatus.RUNNING;
    public abstract @NotNull String getName();
    public abstract @NotNull RunnableType getType();

    public RunnableStatus getStatus() {
        return this.status;
    }

    void setStatus(RunnableStatus status) {
        this.status = status;
    }

    public enum RunnableStatus {
        RUNNING,
        DAMAGED,
        ENDING,
        ENDED,
        FAILED
    }
    public enum RunnableType {
        REPEATED,
        ONESHOT
    }
}