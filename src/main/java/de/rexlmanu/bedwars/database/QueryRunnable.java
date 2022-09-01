package de.rexlmanu.bedwars.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class QueryRunnable implements Runnable {

    private final PreparedStatement query;
    private final Callback<ResultSet> callback;
    private final DatabaseManager manager;
    private final Lock lock;
    private final Condition condition;

    public QueryRunnable(PreparedStatement query, Callback<ResultSet> callback, DatabaseManager manager, Lock lock, Condition condition) {
        this.query = query;
        this.callback = callback;
        this.manager = manager;
        this.lock = lock;
        this.condition = condition;
    }

    @Override
    public void run() {
        this.manager.checkConnection();

        PreparedStatement statement = this.query;

        try {

            ResultSet resultSet = statement.executeQuery();

            if (resultSet == null) {
                this.callback.onFailure(new NullPointerException());
            } else {
                this.callback.onSuccess(resultSet);
                if (this.condition != null && this.lock != null) {
                    this.lock.lock();
                    this.condition.signal();
                    this.lock.unlock();
                }
            }

        } catch (SQLException e) {
            this.callback.onFailure(e.getCause());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    this.callback.onFailure(e.getCause());
                }
            }
        }
    }
}
