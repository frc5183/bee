package org.team5183.beeapi.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.team5183.beeapi.ConfigurationParser;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.entities.UserEntity;

import java.sql.SQLException;

public class Database {
    private static JdbcPooledConnectionSource connectionSource;

    private static Dao<UserEntity, Long> userDao;
    private static Dao<ItemEntity, Long> itemDao;

    public static void init() throws SQLException {
        ConfigurationParser.getConfiguration();

        connectionSource = new JdbcPooledConnectionSource(ConfigurationParser.getConfiguration().databaseUrl);

        connectionSource.setMaxConnectionsFree(ConfigurationParser.getConfiguration().databaseMaxConnections);

        TableUtils.createTableIfNotExists(connectionSource, UserEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, ItemEntity.class);

        userDao = DaoManager.createDao(connectionSource, UserEntity.class);
        itemDao = DaoManager.createDao(connectionSource, ItemEntity.class);
    }

    public static JdbcPooledConnectionSource getConnectionSource() {
        return connectionSource;
    }

    public static Dao<UserEntity, Long> getUserDao() {
        return userDao;
    }

    public static Dao<ItemEntity, Long> getItemDao() {
        return itemDao;
    }
}
