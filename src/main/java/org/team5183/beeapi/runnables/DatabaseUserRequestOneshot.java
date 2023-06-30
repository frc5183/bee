package org.team5183.beeapi.runnables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.entities.UserEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatabaseUserRequestOneshot extends OneshotRunnable {
    private static final Logger logger = LogManager.getLogger(DatabaseUserRequestOneshot.class);

    private final PreparedStmt<UserEntity> statement;
    private final Dao<UserEntity, Long> dao;

    private CompletableFuture<UserEntity> singleFuture;
    private CompletableFuture<List<UserEntity>> multipleFuture;
    private CompletableFuture<Integer> basicFuture;

    public DatabaseUserRequestOneshot(PreparedStmt<UserEntity> statement) {
        super();
        this.statement = statement;
        this.dao = DatabaseRunnable.getUserDao();
    }

    public void setSingleFuture(CompletableFuture<UserEntity> future) {
        this.singleFuture = future;
    }

    public void setMultipleFuture(CompletableFuture<List<UserEntity>> future) {
        this.multipleFuture = future;
    }

    public void setBasicFuture(CompletableFuture<Integer> future) {
        this.basicFuture = future;
    }

    @Override
    public void run() {
        if (this.singleFuture == null && this.multipleFuture == null && this.basicFuture == null) {
            this.setStatus(RunnableStatus.FAILED);
            logger.error("No futures were set for the request.");
            return;
        }

        if (this.singleFuture != null && this.statement.getType() == StatementBuilder.StatementType.SELECT) {
            try {
                this.singleFuture.complete(dao.queryForFirst((PreparedQuery<UserEntity>) statement));
            } catch (SQLException e) {
                this.singleFuture.completeExceptionally(e);
            }
        }

        if (this.multipleFuture != null && this.statement.getType() == StatementBuilder.StatementType.SELECT) {
            try {
                this.multipleFuture.complete(dao.query((PreparedQuery<UserEntity>) statement));
            } catch (SQLException e) {
                this.multipleFuture.completeExceptionally(e);
            }
        }

        if (this.basicFuture != null) {
            switch (this.statement.getType()) {
                case UPDATE:
                    try {
                        this.basicFuture.complete(dao.update((PreparedUpdate<UserEntity>) statement));
                    } catch (SQLException e) {
                        this.basicFuture.completeExceptionally(e);
                    }
                case DELETE:
                    try {
                        this.basicFuture.complete(dao.delete((PreparedDelete<UserEntity>) statement));
                    } catch (SQLException e) {
                        this.basicFuture.completeExceptionally(e);
                    }
                case EXECUTE:
                    try {
                        this.basicFuture.complete(dao.executeRawNoArgs(statement.getStatement()));
                    } catch (SQLException e) {
                        this.basicFuture.completeExceptionally(e);
                    }
                default:

            }
        }

        this.setStatus(RunnableStatus.ENDED);
    }

    @Override
    public @NotNull String getName() {
        return "Database User Request";
    }
}
