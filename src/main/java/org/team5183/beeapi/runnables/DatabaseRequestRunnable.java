package org.team5183.beeapi.runnables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.*;
import com.j256.ormlite.table.TableUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.entities.UserEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

// todo: make this better (im sure it doesnt even work right but i haven't really tested)
public class DatabaseRequestRunnable extends EndableRunnable {
    private static Logger logger = LogManager.getLogger(DatabaseRequestRunnable.class);

    private static CompletableFuture<Boolean> ready = new CompletableFuture<>();

    // Item DAO for interacting with specific tables.
    private static Dao<ItemEntity, Long> itemDao;
    private static Dao<UserEntity, Long> userDao;

    // Caches.
    private static final LinkedBlockingQueue<PreparedStmt<ItemEntity>> itemCache = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<PreparedStmt<UserEntity>> userCache = new LinkedBlockingQueue<>();

    private static final ConcurrentHashMap<PreparedStmt<ItemEntity>, CompletableFuture<ItemEntity>> itemFutureCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PreparedStmt<UserEntity>, CompletableFuture<UserEntity>> userFutureCache = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<PreparedStmt<ItemEntity>, CompletableFuture<List<ItemEntity>>> itemFutureListCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<PreparedStmt<UserEntity>, CompletableFuture<List<UserEntity>>> userFutureListCache = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<PreparedStmt, CompletableFuture<Boolean>> basicFutureCache = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "DatabaseRequestRunnable";
    }

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

        ready.complete(true);

        // TODO: make queries run faster idk
        // Begin loop.
        while (this.status != RunnableStatus.ENDED || this.status != RunnableStatus.ENDING) {
            try {
                // Dump all caches.
                for (int i = 0; i < itemCache.size(); i++)
                    dumpItemCache(itemCache.poll());
                for (int i = 0; i < userCache.size(); i++)
                    dumpUserCache(userCache.poll());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.status == RunnableStatus.ENDING) {
            try {
                // Dump all caches.
                for (int i = 0; i < itemCache.size(); i++) dumpItemCache(itemCache.poll());
                for (int i = 0; i < userCache.size(); i++) dumpUserCache(userCache.poll());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

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

    private synchronized void dumpItemCache(@NotNull PreparedStmt<ItemEntity> itemStmt) throws SQLException {
        if (itemFutureCache.containsKey(itemStmt) && itemStmt.getType() == StatementBuilder.StatementType.SELECT) {
            CompletableFuture<ItemEntity> future = itemFutureCache.get(itemStmt);
            try {
                future.complete(itemDao.queryForFirst((PreparedQuery<ItemEntity>) itemStmt));
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        } else if (itemFutureListCache.containsKey(itemStmt) && itemStmt.getType() == StatementBuilder.StatementType.SELECT) {
            CompletableFuture<List<ItemEntity>> future = itemFutureListCache.get(itemStmt);
            try {
                future.complete(itemDao.query((PreparedQuery<ItemEntity>) itemStmt));
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        } else {
            switch (itemStmt.getType()) {
                case DELETE: itemDao.delete((PreparedDelete<ItemEntity>) itemStmt);
                case UPDATE: itemDao.update((PreparedUpdate<ItemEntity>) itemStmt);
                default:
                    logger.fatal("Executing raw statement, this probably shouldn't be happening and might be vulnerable to SQL injection! Type: " + itemStmt.getType() + ". Statement Contents: " + itemStmt.getStatement());
                    itemDao.executeRaw(itemStmt.getStatement());
            }
        }
    }

    private synchronized void dumpUserCache(@NotNull PreparedStmt<UserEntity> userStmt) throws SQLException {
        if (userFutureCache.containsKey(userStmt) && userStmt.getType().isOkForQuery()) {
            CompletableFuture<UserEntity> future = userFutureCache.get(userStmt);
            try {
                future.complete(userDao.queryForFirst((PreparedQuery<UserEntity>) userStmt));
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        } else if (userFutureListCache.containsKey(userStmt) && userStmt.getType().isOkForQuery()) {
            CompletableFuture<List<UserEntity>> future = userFutureListCache.get(userStmt);
            try {
                future.complete(userDao.query((PreparedQuery<UserEntity>) userStmt));
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        } else {
            try {
                switch (userStmt.getType()) {
                    case DELETE:
                        userDao.delete((PreparedDelete<UserEntity>) userStmt);
                    case UPDATE:
                        userDao.update((PreparedUpdate<UserEntity>) userStmt);
                    default:
                        logger.fatal("Executing raw statement, this probably shouldn't be happening and might be vulnerable to SQL injection! Type: " + userStmt.getType() + ". Statement Contents: " + userStmt.getStatement());
                        itemDao.executeRaw(userStmt.getStatement());
                }
                basicFutureCache.get(userStmt).complete(null);
            } catch (SQLException e) {
                basicFutureCache.get(userStmt).completeExceptionally(e);
            }
        }
    }

    public static synchronized Dao<ItemEntity, Long> getItemDao() {
        return itemDao;
    }

    public static synchronized Dao<UserEntity, Long> getUserDao() {
        return userDao;
    }

    public static synchronized CompletableFuture<Boolean> itemStatement(@NotNull PreparedStmt<ItemEntity> statement) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        itemCache.offer(statement);
        basicFutureCache.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<ItemEntity> itemQuery(@NotNull PreparedQuery<ItemEntity> statement) {
        CompletableFuture<ItemEntity> future = new CompletableFuture<>();
        itemCache.offer(statement);
        itemFutureCache.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<List<ItemEntity>> itemQueryMultiple(@NotNull PreparedQuery<ItemEntity> statement) {
        CompletableFuture<List<ItemEntity>> future = new CompletableFuture<>();
        itemCache.offer(statement);
        itemFutureListCache.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<Boolean> userStatement(@NotNull PreparedStmt<UserEntity> statement) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        userCache.offer(statement);
        basicFutureCache.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<UserEntity> userQuery(@NotNull PreparedQuery<UserEntity> statement) {
        CompletableFuture<UserEntity> future = new CompletableFuture<>();
        userCache.offer(statement);
        userFutureCache.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<List<UserEntity>> userQueryMultiple(@NotNull PreparedQuery<UserEntity> statement) {
        CompletableFuture<List<UserEntity>> future = new CompletableFuture<>();
        userCache.offer(statement);
        userFutureListCache.put(statement, future);
        return future;
    }

    public static synchronized CompletableFuture<Boolean> getReady() {
        return ready;
    }
}
