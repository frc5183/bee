package org.team5183.beeapi.runnables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.entities.ItemEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatabaseItemRequestOneshot extends OneshotRunnable {
    private static final Logger logger = LogManager.getLogger(DatabaseItemRequestOneshot.class);

    private final PreparedStmt<ItemEntity> statement;
    private final Dao<ItemEntity, Long> dao;

    private CompletableFuture<ItemEntity> singleFuture;
    private CompletableFuture<List<ItemEntity>> multipleFuture;
    private CompletableFuture<Integer> basicFuture;

    public DatabaseItemRequestOneshot(PreparedStmt<ItemEntity> statement) {
        super();
        this.statement = statement;
        this.dao = DatabaseRunnable.getItemDao();
    }

    public void setSingleFuture(CompletableFuture<ItemEntity> future) {
        this.singleFuture = future;
    }

    public void setMultipleFuture(CompletableFuture<List<ItemEntity>> future) {
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
                this.singleFuture.complete(dao.queryForFirst((PreparedQuery<ItemEntity>) statement));
            } catch (SQLException e) {
                this.singleFuture.completeExceptionally(e);
            }
        }

        if (this.multipleFuture != null && this.statement.getType() == StatementBuilder.StatementType.SELECT) {
            try {
                this.multipleFuture.complete(dao.query((PreparedQuery<ItemEntity>) statement));
            } catch (SQLException e) {
                this.multipleFuture.completeExceptionally(e);
            }
        }

        if (this.basicFuture != null) {
            switch (this.statement.getType()) {
                case UPDATE:
                    try {
                        this.basicFuture.complete(dao.update((PreparedUpdate<ItemEntity>) statement));
                    } catch (SQLException e) {
                        this.basicFuture.completeExceptionally(e);
                    }
                case DELETE:
                    try {
                        this.basicFuture.complete(dao.delete((PreparedDelete<ItemEntity>) statement));
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
        return "Database Item Request";
    }
}
