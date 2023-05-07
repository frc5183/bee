package org.team5183.beeapi.util;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.entities.UserEntity;

import java.sql.SQLException;
import java.util.List;

public class Database {
    private static final Logger logger = LogManager.getLogger(Database.class);

    private static Dao<ItemEntity, Long> itemDao;
    private static Dao<UserEntity, Long> userDao;

    public static void init() throws SQLException {
        logger.info(System.getenv("DATABASE_URL"));
        JdbcPooledConnectionSource connectionSource = new JdbcPooledConnectionSource(System.getenv("DATABASE_URL") == null ? "jdbc:sqlite:beeapi.db" : System.getenv("DATABASE_URL"));
        connectionSource.setMaxConnectionsFree(System.getenv("DATABASE_MAX_CONNECTIONS") == null ? 10 : Integer.parseInt(System.getenv("DATABASE_MAX_CONNECTIONS")));

        TableUtils.createTableIfNotExists(connectionSource, ItemEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, UserEntity.class);

        itemDao = DaoManager.createDao(connectionSource, ItemEntity.class);
        userDao = DaoManager.createDao(connectionSource, UserEntity.class);
    }

    @Nullable
    public static ItemEntity getItemEntity(Long id) throws SQLException {
        return itemDao.queryForId(id);
    }

    @Nullable
    public static List<ItemEntity> getAllItemEntities() throws SQLException {
        return itemDao.queryForAll();
    }

    public static void updateItemEntity(ItemEntity item) throws SQLException {
        itemDao.update(item);
    }

    public static void deleteItemEntity(ItemEntity item) throws SQLException {
        itemDao.delete(item);
    }

    public static void upsertItemEntity(ItemEntity item) throws SQLException {
        itemDao.createOrUpdate(item);
    }

    @Nullable
    public static UserEntity getUserEntity(Long id) throws SQLException {
        return userDao.queryForId(id);
    }

    @Nullable
    public static UserEntity getUserEntityByLogin(String login) throws SQLException {
        return userDao.queryBuilder().where().eq("login", login).queryForFirst();
    }

    @Nullable
    public static UserEntity getUserEntityByEmail(String email) throws SQLException {
        return userDao.queryBuilder().where().eq("email", email).queryForFirst();
    }

    @Nullable
    public static UserEntity getUserEntityByToken(String token) throws SQLException {
        return userDao.queryBuilder().where().eq("token", token).queryForFirst();
    }

    public static void updateUserEntity(UserEntity user) throws SQLException {
        userDao.update(user);
    }

    public static void deleteUserEntity(UserEntity user) throws SQLException {
        userDao.delete(user);
    }

    public static void upsertUserEntity(UserEntity user) throws SQLException {
        userDao.createOrUpdate(user);
    }
}
