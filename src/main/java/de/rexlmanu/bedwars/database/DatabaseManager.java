package de.rexlmanu.bedwars.database;

import lombok.Getter;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;

public class DatabaseManager {

    @Getter
    private static final ExecutorService service = Executors.newCachedThreadPool();

    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;
    private Connection connection;

    public DatabaseManager(String host, String database, String username, String password, int port) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public DatabaseManager(DatabaseConnection databaseConnection) {
        this(
                databaseConnection.getHostName(),
                databaseConnection.getDatabase(),
                databaseConnection.getUserName(),
                databaseConnection.getPassword(),
                databaseConnection.getPort()
        );
    }

    public PreparedStatementBuilder builder(String update) {
        return new PreparedStatementBuilder(this, update);
    }


    public CompletableFuture<Void> update(PreparedStatement preparedStatement) {
        return CompletableFuture.runAsync(() -> service.execute(() -> {
            try {
                syncUpdate(preparedStatement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    public final PreparedStatement prepareStatement(String update) {
        try {
            return this.connection.prepareStatement(update);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void syncUpdate(PreparedStatement preparedStatement) throws SQLException {
        this.checkConnection();

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void query(PreparedStatement query, Callback<ResultSet> callback, Lock lock, Condition condition) {
        this.service.execute(new QueryRunnable(query, callback, this, lock, condition));
    }

    public void update(String query) {
        this.checkConnection();
        update(new PreparedStatementBuilder(this, query).build());
    }

    public ResultSet syncExecute(PreparedStatement statement) {
        this.checkConnection();
        try {
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (! statement.isClosed()) {
                    try {
                        statement.close();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }

                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public void execute(PreparedStatement statement, BiConsumer<ResultSet, Throwable> biConsumer) {
        this.checkConnection();
        try {
            ResultSet resultSet = statement.executeQuery();
            biConsumer.accept(resultSet, null);
            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            biConsumer.accept(null, e);
        }
    }

    public void checkConnection() {
        if (! isConnected()) openConnection();
    }

    /**
     * Open the connection for the Database.
     *
     * @return returns the instance.
     */
    public DatabaseManager openConnection() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true", this.username, this.password);
        } catch (SQLException e) {
            System.out.println("Es konnte keine Database-Connection aufgebunden werden.");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Open the connection for the Database and run a runnable.
     *
     * @param runnable for creating tables
     * @return returns the instance.
     */
    public DatabaseManager openConnection(Runnable runnable) {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true", this.username, this.password);
            runnable.run();
        } catch (SQLException e) {
            System.out.println("Es konnte keine Database-Connection aufgebunden werden.");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * check if the connection is connected.
     *
     * @return one boolean that say if connected.
     */
    public boolean isConnected() {
        try {
            return this.connection != null && ! this.connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        if (isConnected()) {
            try {
                this.connection.close();
                this.service.shutdown();
                this.connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ;
        }
    }
}
