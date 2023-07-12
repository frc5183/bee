package org.team5183.beeapi.runnables;

import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.ConfigurationParser;

public abstract class RepeatedRunnable extends NamedRunnable {
    @Override
    public void run() {
        init();
        while (this.status == RunnableStatus.RUNNING || this.status == RunnableStatus.DAMAGED) {
            loop();
            if (ConfigurationParser.getConfiguration().threadDelay > 0) {
                try {
                    Thread.sleep(ConfigurationParser.getConfiguration().threadDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        end();
        this.status = this.status == RunnableStatus.FAILED ? RunnableStatus.FAILED : RunnableStatus.ENDED;
    }

    abstract void init();
    abstract void loop();
    abstract void end();

    public void shutdown() {
        this.status = RunnableStatus.ENDING;
    }

    @Override
    public @NotNull RunnableType getType() {
        return RunnableType.REPEATED;
    }
}
