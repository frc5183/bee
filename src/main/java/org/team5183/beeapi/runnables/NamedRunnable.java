package org.team5183.beeapi.runnables;

import org.jetbrains.annotations.NotNull;

public interface NamedRunnable extends Runnable {
    
    @NotNull String getName();
}
