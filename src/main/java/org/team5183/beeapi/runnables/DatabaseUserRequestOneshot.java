package org.team5183.beeapi.runnables;

import com.j256.ormlite.dao.Dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.entities.UserEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DatabaseUserRequestOneshot extends OneshotRunnable {
    private static final Logger logger = LogManager.getLogger(DatabaseUserRequestOneshot.class);

    private final DatabaseRunnable.DatabaseRequest<UserEntity> request;
    private final CompletableFuture<Optional<List<UserEntity>>> future;

    public DatabaseUserRequestOneshot(DatabaseRunnable.DatabaseRequest<UserEntity> request, CompletableFuture<Optional<List<UserEntity>>> future) {
        this.request = request;
        this.future = future;
    }

    @Override
    public void run() {
        Dao<UserEntity, Long> dao = DatabaseRunnable.getUserDao();
        try {
            switch (request.getType()) {
                case SELECT -> {
                    if (request.getQuery() == null)
                        throw new NullPointerException("Query cannot be null on a SELECT request");
                    List<UserEntity> entities = dao.query(request.getQuery());
                    future.complete(Optional.of(entities));
                }
                case DELETE -> {
                    if (request.getDelete() == null)
                        throw new NullPointerException("Delete cannot be null on a DELETE request");
                    dao.delete(request.getDelete());
                    future.complete(Optional.empty());
                }
                case INSERT -> {
                    if (request.getEntity() == null)
                        throw new NullPointerException("Entity cannot be null on a INSERT request");
                    dao.createIfNotExists(request.getEntity());
                    future.complete(Optional.empty());
                }
                case UPDATE -> {
                    if (request.getEntity() == null)
                        throw new NullPointerException("Update cannot be null on a UPDATE request");
                    dao.update(request.getEntity());
                    future.complete(Optional.empty());
                }
                case UPSERT -> {
                    if (request.getEntity() == null)
                        throw new NullPointerException("Entity cannot be null on a UPSERT request");
                    dao.createOrUpdate(request.getEntity());
                    future.complete(Optional.empty());
                }
                default -> throw new IllegalArgumentException("Invalid request type");
            }
        } catch (SQLException e) {
            future.completeExceptionally(e);
            this.status = RunnableStatus.FAILED;
            return;
        }

        this.status = RunnableStatus.ENDED;
    }

    @Override
    public @NotNull String getName() {
        return "Database User Request";
    }
}
