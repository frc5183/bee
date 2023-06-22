package org.team5183.beeapi.threading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.runnables.EndableRunnable;
import org.team5183.beeapi.runnables.RunnableStatus;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadingManager extends Thread {
    private static final Logger logger = LogManager.getLogger(ThreadingManager.class);

    private static final LinkedBlockingQueue<EndableRunnable> queue = new LinkedBlockingQueue<>();
    private static final HashMap<EndableRunnable, Thread> threads = new HashMap<>();

    private static final HashMap<EndableRunnable, Integer> endAttempts = new HashMap<>();

    private static final int maxThreads = (System.getenv("maxThreads") == null || System.getenv("maxThreads").isEmpty() || System.getenv("maxThreads").isBlank()) ? 10 : Integer.parseInt(System.getenv("maxThreads"));
    private static final int maxEndAttempts = (System.getenv("maxEndAttempts") == null || System.getenv("maxEndAttempts").isEmpty() || System.getenv("maxEndAttempts").isBlank()) ? 5 : Integer.parseInt(System.getenv("maxEndAttempts"));

    private static final HashMap<EndableRunnable, Long> lastDamagedMessage = new HashMap<>();

    private static boolean running = true;
    private static boolean ended = false;

    @Override
    public void run() {
        while (true) {
            if (!running) {
                for (EndableRunnable runnable : threads.keySet()) {
                    if (runnable.getStatus() != RunnableStatus.ENDED || runnable.getStatus() != RunnableStatus.ENDING) {
                        endAttempts.putIfAbsent(runnable, 0);
                        if (endAttempts.get(runnable) >= 5) {
                            logger.error("Thread " + runnable.getClass().getName() + " has failed to end after " + maxEndAttempts +" attempts, forcibly ending.");
                            threads.get(runnable).interrupt();
                        }
                        endAttempts.put(runnable, endAttempts.get(runnable) + 1);
                        runnable.shutdown();
                    }
                    if (runnable.getStatus() == RunnableStatus.ENDED) threads.remove(runnable);
                }
                if (threads.size() == 0) {
                    ended = true;
                    break;
                }
            }

            for (EndableRunnable runnable : threads.keySet()) {
                if (runnable.getStatus() == RunnableStatus.ENDED) threads.remove(runnable);
                if (runnable.getStatus() == RunnableStatus.DAMAGED && System.currentTimeMillis() - lastDamagedMessage.get(runnable) < 60000) {
                    logger.warn("Thread " + runnable.getClass().getName() + " is marked as damaged.");
                    lastDamagedMessage.put(runnable, System.currentTimeMillis());
                }

                if (runnable.getStatus() != RunnableStatus.DAMAGED) lastDamagedMessage.remove(runnable);
            }

            if (threads.size() >= maxThreads) continue;
            try {
                EndableRunnable runnable = queue.take();
                Thread thread = new Thread(runnable);
                threads.put(runnable, thread);
                thread.start();
            } catch (InterruptedException e) {
                logger.error("Error while taking from queue", e);
            }
        }
    }

    public static void addTask(EndableRunnable runnable) {
        queue.add(runnable);
    }

    public static synchronized CompletableFuture<String> shutdown() {
        CompletableFuture<String> future = new CompletableFuture<>();
        logger.info("Shutting down threading manager");
        running = false;
        while (true) {
            if (ended) {
                break;
            }
        }
        future.complete("Threading manager has been shut down");
        return future;
    }
}
