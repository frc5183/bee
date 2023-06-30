package org.team5183.beeapi.runnables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.*;
import com.j256.ormlite.table.TableUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.entities.UserEntity;
import org.team5183.beeapi.threading.ThreadingManager;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

// todo: make this better (im sure it doesnt even work right but i haven't really tested)
public class DatabaseRunnable extends RepeatedRunnable {
    private static final Logger logger = LogManager.getLogger(DatabaseRunnable.class);

    private static final Callback.Completable ready = new Callback.Completable();

    // Item DAO for interacting with specific tables.
    private static Dao<ItemEntity, Long> itemDao;
    private static Dao<UserEntity, Long> userDao;

    // Caches.
    private static final LinkedBlockingQueue<PreparedStmt<ItemEntity>> itemCache = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<PreparedStmt<UserEntity>> userCache = new LinkedBlockingQueue<>();

    private static final ConcurrentHashMap<PreparedStmt<ItemEntity>, CompletableFuture<ItemEntity>> itemFutures = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PreparedStmt<UserEntity>, CompletableFuture<UserEntity>> userFutures = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<PreparedStmt<ItemEntity>, CompletableFuture<List<ItemEntity>>> itemMultiFutures = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PreparedStmt<UserEntity>, CompletableFuture<List<UserEntity>>> userMultiFutures = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<PreparedStmt<ItemEntity>, CompletableFuture<Integer>> basicItemFutures = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PreparedStmt<UserEntity>, CompletableFuture<Integer>> basicUserFutures = new ConcurrentHashMap<>();

    @Override
    public synchronized void run() {
        // Initialize database.
        JdbcPooledConnectionSource connectionSource = null;
        try {
            // Initialize connection source.
            connectionSource = new JdbcPooledConnectionSource(System.getenv("DATABASE_URL") == null ? "jdbc:sqlite:beeapi.db" : System.getenv("DATABASE_URL"));
            connectionSource.setMaxConnectionsFree(System.getenv("DATABASE_MAX_CONNECTIONS") == null ? 10 : Integer.parseInt(System.getenv("DATABASE_MAX_CONNECTIONS")));

            // Create tables if not exists.
            TableUtils.createTableIfNotExists(connectionSource, ItemEntity.class);
            TableUtils.createTableIfNotExists(connectionSource, UserEntity.class);

            // Initialize DAOs.
            itemDao = DaoManager.createDao(connectionSource, ItemEntity.class);
            userDao = DaoManager.createDao(connectionSource, UserEntity.class);
        } catch (SQLException e) {
            ready.completeExceptionally(e);
            throw new RuntimeException(e);
        }

        ready.succeeded();

        // TODO: make queries run faster idk
        // Begin loop.
        while (this.status != RunnableStatus.ENDED || this.status != RunnableStatus.ENDING) {
            // todo move these to now deprecated dumpItemCache
            for (int i = 0; i < itemCache.size(); i++) {
                PreparedStmt<ItemEntity> stmt = itemCache.poll();
                DatabaseItemRequestOneshot itemRequest = new DatabaseItemRequestOneshot(stmt);
                if (itemFutures.containsKey(stmt)) {
                    itemRequest.setSingleFuture(itemFutures.get(stmt));
                } else if (itemMultiFutures.containsKey(stmt)) {
                    itemRequest.setMultipleFuture(itemMultiFutures.get(stmt));
                } else if (basicItemFutures.containsKey(stmt)) {
                    itemRequest.setBasicFuture(basicItemFutures.get(stmt));
                }
                ThreadingManager.addTask(itemRequest);
            }
            for (int i = 0; i < userCache.size(); i++) {
                PreparedStmt<UserEntity> stmt = userCache.poll();
                DatabaseUserRequestOneshot userRequest = new DatabaseUserRequestOneshot(stmt);
                if (userFutures.containsKey(stmt)) {
                    userRequest.setSingleFuture(userFutures.get(stmt));
                } else if (userMultiFutures.containsKey(stmt)) {
                    userRequest.setMultipleFuture(userMultiFutures.get(stmt));
                } else if (basicUserFutures.containsKey(stmt)) {
                    userRequest.setBasicFuture(basicUserFutures.get(stmt));
                }
                ThreadingManager.addTask(userRequest);
            }

            // busy wait for 10ms
            try {
                wait(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.status == RunnableStatus.ENDING) {
//            try {
//                // Dump all caches.
//                for (int i = 0; i < itemCache.size(); i++) dumpItemCache(itemCache.poll());
//                for (int i = 0; i < userCache.size(); i++) dumpUserCache(userCache.poll());
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }

            // Close connection source.
            try {
                connectionSource.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        this.status = RunnableStatus.ENDED;
    }

    @Override
    public synchronized void shutdown() {
        this.status = RunnableStatus.ENDING; // Set status to ending.
        notifyAll(); // Notify all threads to wake up from sleep/wait.
    }

//    private synchronized void dumpItemCache(@NotNull PreparedStmt<ItemEntity> itemStmt) throws SQLException {
//        if (itemFutures.containsKey(itemStmt) && itemStmt.getType() == StatementBuilder.StatementType.SELECT) {
//            CompletableFuture<ItemEntity> future = itemFutures.get(itemStmt);
//            try {
//                future.complete(itemDao.queryForFirst((PreparedQuery<ItemEntity>) itemStmt));
//            } catch (SQLException e) {
//                future.completeExceptionally(e);
//            }
//        } else if (itemMultiFutures.containsKey(itemStmt) && itemStmt.getType() == StatementBuilder.StatementType.SELECT) {
//            CompletableFuture<List<ItemEntity>> future = itemMultiFutures.get(itemStmt);
//            try {
//                future.complete(itemDao.query((PreparedQuery<ItemEntity>) itemStmt));
//            } catch (SQLException e) {
//                future.completeExceptionally(e);
//            }
//        } else {
//            switch (itemStmt.getType()) {
//                case DELETE: itemDao.delete((PreparedDelete<ItemEntity>) itemStmt);
//                case UPDATE: itemDao.update((PreparedUpdate<ItemEntity>) itemStmt);
//                default:
//                    logger.fatal("Executing raw statement, this probably shouldn't be happening and might be vulnerable to SQL injection! RunnableType: " + itemStmt.getType() + ". Statement Contents: " + itemStmt.getStatement());
//                    itemDao.executeRaw(itemStmt.getStatement());
//            }
//        }
//    }

//    private synchronized void dumpUserCache(@NotNull PreparedStmt<UserEntity> userStmt) throws SQLException {
//        if (userFutures.containsKey(userStmt) && userStmt.getType() == StatementBuilder.StatementType.SELECT) {
//            CompletableFuture<UserEntity> future = userFutures.get(userStmt);
//            try {
//                future.complete(userDao.queryForFirst((PreparedQuery<UserEntity>) userStmt));
//            } catch (SQLException e) {
//                future.completeExceptionally(e);
//            }
//        } else if (userMultiFutures.containsKey(userStmt) && userStmt.getType() == StatementBuilder.StatementType.SELECT) {
//            CompletableFuture<List<UserEntity>> future = userMultiFutures.get(userStmt);
//            try {
//                future.complete(userDao.query((PreparedQuery<UserEntity>) userStmt));
//            } catch (SQLException e) {
//                future.completeExceptionally(e);
//            }
//        } else {
//            try {
//                switch (userStmt.getType()) {
//                    case DELETE:
//                        userDao.delete((PreparedDelete<UserEntity>) userStmt);
//                    case UPDATE:
//                        userDao.update((PreparedUpdate<UserEntity>) userStmt);
//                    default:
//                        logger.fatal("Executing raw statement, this probably shouldn't be happening and might be vulnerable to SQL injection! RunnableType: " + userStmt.getType() + ". Statement Contents: " + userStmt.getStatement());
//                        itemDao.executeRaw(userStmt.getStatement());
//                }
//                basicFutures.get(userStmt).complete(null);
//            } catch (SQLException e) {
//                basicFutures.get(userStmt).completeExceptionally(e);
//            }
//        }
//    }

    public static synchronized Dao<ItemEntity, Long> getItemDao() {
        return itemDao;
    }

    public static synchronized Dao<UserEntity, Long> getUserDao() {
        return userDao;
    }

    public static synchronized CompletableFuture<Integer> itemStatement(@NotNull PreparedStmt<ItemEntity> statement) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        itemCache.offer(statement);
        basicItemFutures.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<ItemEntity> itemQuery(@NotNull PreparedQuery<ItemEntity> statement) {
        CompletableFuture<ItemEntity> future = new CompletableFuture<>();
        itemCache.offer(statement);
        itemFutures.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<List<ItemEntity>> itemQueryMultiple(@NotNull PreparedQuery<ItemEntity> statement) {
        CompletableFuture<List<ItemEntity>> future = new CompletableFuture<>();
        itemCache.offer(statement);
        itemMultiFutures.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<Integer> userStatement(@NotNull PreparedStmt<UserEntity> statement) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        userCache.offer(statement);
        basicUserFutures.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<UserEntity> userQuery(@NotNull PreparedQuery<UserEntity> statement) {
        CompletableFuture<UserEntity> future = new CompletableFuture<>();
        userCache.offer(statement);
        userFutures.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<List<UserEntity>> userQueryMultiple(@NotNull PreparedQuery<UserEntity> statement) {
        CompletableFuture<List<UserEntity>> future = new CompletableFuture<>();
        userCache.offer(statement);
        userMultiFutures.put(statement, future);
        return future;
    }

    public static synchronized Callback.Completable getReady() {
        return ready;
    }

    @Override
    public @NotNull String getName() {
        return "Database";
    }
}
