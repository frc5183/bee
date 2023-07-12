package org.team5183.beeapi.runnables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.table.TableUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.ConfigurationParser;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.entities.UserEntity;
import org.team5183.beeapi.threading.ThreadingManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

// todo: make this better (im sure it doesnt even work right but i haven't really tested)
public class DatabaseRunnable extends RepeatedRunnable {
    private static final Logger logger = LogManager.getLogger(DatabaseRunnable.class);

    private static final Callback.Completable ready = new Callback.Completable();

    // Connection source
    private static JdbcPooledConnectionSource connectionSource;

    // Item DAO for interacting with specific tables.
    private static Dao<ItemEntity, Long> itemDao;
    private static Dao<UserEntity, Long> userDao;

    // Caches.
    private static final LinkedBlockingQueue<DatabaseRequest<ItemEntity>> itemQueue = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<DatabaseRequest<UserEntity>> userQueue = new LinkedBlockingQueue<>();

    private static final ConcurrentHashMap<DatabaseRequest<ItemEntity>, CompletableFuture<Optional<List<ItemEntity>>>> itemFutures = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<DatabaseRequest<UserEntity>, CompletableFuture<Optional<List<UserEntity>>>> userFutures = new ConcurrentHashMap<>();

    @Override
    void init() {
        connectionSource = null;
        try {
            // Initialize connection source.
            connectionSource = new JdbcPooledConnectionSource(ConfigurationParser.getConfiguration().databaseUrl);
            connectionSource.setMaxConnectionsFree(ConfigurationParser.getConfiguration().databaseMaxConnections);

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
    }

    @Override
    void loop() {
        drainQueues();
    }

    @Override
    void end() {
        drainQueues();
        try {
            connectionSource.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void drainQueues() {
        List<DatabaseRequest<ItemEntity>> itemRequests = new ArrayList<>(itemQueue.size());
        itemQueue.drainTo(itemRequests);
        for (DatabaseRequest<ItemEntity> request : itemRequests) {
            ThreadingManager.addTask(
                    new DatabaseItemRequestOneshot(request, itemFutures.get(request))
            );
        }

        List<DatabaseRequest<UserEntity>> userRequests = new ArrayList<>(userQueue.size());
        userQueue.drainTo(userRequests);
        for (DatabaseRequest<UserEntity> request : userRequests) {
            ThreadingManager.addTask(
                    new DatabaseUserRequestOneshot(request, userFutures.get(request))
            );
        }
    }

    public static synchronized Dao<ItemEntity, Long> getItemDao() {
        return itemDao;
    }

    public static synchronized Dao<UserEntity, Long> getUserDao() {
        return userDao;
    }

    public static synchronized CompletableFuture<Optional<List<ItemEntity>>> itemRequest(DatabaseRequest<ItemEntity> request) {
        CompletableFuture<Optional<List<ItemEntity>>> future = new CompletableFuture<>();
        itemFutures.put(request, future);
        itemQueue.offer(request);
        return future;
    }

    public static synchronized CompletableFuture<Optional<List<UserEntity>>> userRequest(DatabaseRequest<UserEntity> request) {
        CompletableFuture<Optional<List<UserEntity>>> future = new CompletableFuture<>();
        userFutures.put(request, future);
        userQueue.offer(request);
        return future;
    }


    public static synchronized Callback.Completable getReady() {
        return ready;
    }

    @Override
    public @NotNull String getName() {
        return "Database";
    }

    public static class DatabaseRequest<Entity> {
        private PreparedQuery<Entity> query;
        private PreparedDelete<Entity> delete;
        private PreparedStmt<Entity> statement;
        private Entity entity;
        private final RequestType type;

        public DatabaseRequest(PreparedQuery<Entity> query) {
            this.query = query;
            this.type = RequestType.SELECT;
        }

        public DatabaseRequest(PreparedDelete<Entity> delete) {
            this.delete = delete;
            this.type = RequestType.DELETE;
        }

        public DatabaseRequest(Entity entity, RequestType type) {
            this.entity = entity;
            if (type != RequestType.INSERT && type != RequestType.UPDATE && type != RequestType.UPSERT)
                throw new IllegalArgumentException("RequestType must be INSERT, UPDATE, or UPSERT, instead was " + type.toString());
            this.type = type;
        }

//        public DatabaseRequest(PreparedStmt<Entity> statement) {
//            this.statement = statement;
//            this.type = RequestType.RAW;
//        }

        public enum RequestType {
            SELECT,
            DELETE,
            UPDATE,
            INSERT,
            UPSERT,
            RAW;
        }

        public PreparedQuery<Entity> getQuery() {
            return query;
        }

        public PreparedDelete<Entity> getDelete() {
            return delete;
        }

        public PreparedStmt<Entity> getStatement() {
            return statement;
        }

        public Entity getEntity() {
            return entity;
        }

        public RequestType getType() {
            return type;
        }
    }
}
