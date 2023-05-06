package org.team5183.beeapi.util;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.team5183.beeapi.entities.CheckoutEntity;
import org.team5183.beeapi.entities.ItemEntity;

import java.sql.SQLException;
import java.util.List;

public class Database {
    private static JdbcPooledConnectionSource connectionSource;

    private static Dao<ItemEntity, Long> itemDao;

    public static void init() throws SQLException {
        connectionSource = new JdbcPooledConnectionSource(System.getenv("DATABASE_URL") == null ? "jdbc:sqlite:beeapi.db" : System.getenv("DATABASE_URL"));
        connectionSource.setMaxConnectionsFree(System.getenv("DATABASE_MAX_CONNECTIONS") == null ? 10 : Integer.parseInt(System.getenv("DATABASE_MAX_CONNECTIONS")));

        TableUtils.createTableIfNotExists(connectionSource, ItemEntity.class);

        itemDao = DaoManager.createDao(connectionSource, ItemEntity.class);
    }

    public static ItemEntity getItemEntity(Long id) throws SQLException {
        return itemDao.queryForId(id);
    }

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
}
