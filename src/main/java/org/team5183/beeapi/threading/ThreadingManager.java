package org.team5183.beeapi.threading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.ConfigurationParser;
import org.team5183.beeapi.runnables.NamedRunnable;
import org.team5183.beeapi.runnables.NamedRunnable.RunnableStatus;
import org.team5183.beeapi.runnables.NamedRunnable.RunnableType;
import org.team5183.beeapi.runnables.RepeatedRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadingManager extends Thread {
    private static final Logger logger = LogManager.getLogger(ThreadingManager.class);

    private static final LinkedBlockingQueue<NamedRunnable> queue = new LinkedBlockingQueue<>();
    private static final ConcurrentHashMap<NamedRunnable, Thread> threads = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<NamedRunnable, Integer> endAttempts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<NamedRunnable, Long> lastDamagedMessage = new ConcurrentHashMap<>();

    private static final int maxThreads = ConfigurationParser.getConfiguration().maxThreads;
    private static final int maxEndAttempts = ConfigurationParser.getConfiguration().maxEndAttempts;
    private static final int maxOneshotEndAttempts = ConfigurationParser.getConfiguration().maxOneshotEndAttempts;

    private static RunnableStatus status;

    @Override
    public void run() {
        while (true) {
            if (status == RunnableStatus.ENDING) {
                List<NamedRunnable> finalQueue = new ArrayList<>(queue);
                queue.drainTo(finalQueue);
                finalQueue.forEach(this::startRunnable);

                boolean oneshotDone = false;
                for (NamedRunnable runnable : threads.keySet()) {
                    if (runnable.getType() == RunnableType.ONESHOT) {
                        endAttempts.putIfAbsent(runnable, 0);
                        if (endAttempts.get(runnable) >= maxOneshotEndAttempts) {
                            logger.error("Oneshot thread " + runnable.getName() + " has failed to complete after " + maxOneshotEndAttempts + " attempts, forcibly ending.");
                            threads.get(runnable).interrupt();
                            threads.remove(runnable);
                        } else {
                            endAttempts.put(runnable, endAttempts.get(runnable) + 1);
                        }

                        if (runnable.getStatus() == RunnableStatus.ENDED) threads.remove(runnable);
                        oneshotDone = runnable.getStatus() == RunnableStatus.ENDED;
                    }
                }

                if (!oneshotDone) continue;

                for (NamedRunnable runnable : threads.keySet()) {
                    if (runnable.getType() == RunnableType.REPEATED) {
                        RepeatedRunnable rRunnable = (RepeatedRunnable) runnable;
                        if (rRunnable.getStatus() != RunnableStatus.ENDED) {
                            endAttempts.putIfAbsent(runnable, 0);
                            if (endAttempts.get(runnable) >= maxEndAttempts) {
                                logger.error("Thread " + runnable.getName() + " has failed to end after " + maxEndAttempts + " attempts, forcibly ending.");
                                threads.get(runnable).interrupt();
                                threads.remove(runnable);
                            }
                            endAttempts.put(runnable, endAttempts.get(runnable) + 1);
                            rRunnable.shutdown();
                        }
                        if (runnable.getStatus() == RunnableStatus.ENDED) threads.remove(runnable);
                    }
                }

                if (threads.size() == 0) break;
            } else {
                if (queue.size() > 0) {
                    if (threads.size() >= maxThreads) continue;
                    startRunnable(queue.poll());
                }

                for (NamedRunnable runnable : threads.keySet()) {
                    if (runnable.getStatus() == RunnableStatus.ENDED) threads.remove(runnable);
                    if (runnable.getStatus() == RunnableStatus.DAMAGED && System.currentTimeMillis() - lastDamagedMessage.get(runnable) < 60000) {
                        logger.warn("Thread " + runnable.getClass().getName() + " is marked as damaged.");
                        lastDamagedMessage.put(runnable, System.currentTimeMillis());
                    }
                    if (runnable.getStatus() != RunnableStatus.DAMAGED) lastDamagedMessage.remove(runnable);
                }
            }

            if (ConfigurationParser.getConfiguration().threadDelay > 0) {
                try {
                    sleep(ConfigurationParser.getConfiguration().threadDelay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        status = RunnableStatus.ENDED;
    }

    private synchronized void startRunnable(NamedRunnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(runnable.getName());
        threads.put(runnable, thread);
        thread.start();
    }

    public static synchronized void addTask(NamedRunnable runnable) {
        if (status == RunnableStatus.ENDING || status == RunnableStatus.ENDED) {
            logger.warn("Threading manager is ending, cannot add task " + runnable.getName());
            return;
        }
        queue.add(runnable);
    }

    public static RunnableStatus getStatus() {
        return status;
    }

    public static synchronized void shutdown() {
        logger.info("Shutting down threading manager");
        status = RunnableStatus.ENDING;
    }
}
