package me.soapiee.common.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.Getter;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.stream.Collectors;

public class HikariCPConnection {

    private final String HOST, DATABASE, USERNAME, PASSWORD;
    private final int PORT;

    @Getter private HikariDataSource connection;

    public HikariCPConnection(FileConfiguration config) {
        HOST = config.getString("database.host");
        PORT = config.getInt("database.port");
        DATABASE = config.getString("database.database");
        USERNAME = config.getString("database.username");
        PASSWORD = config.getString("database.password");
    }

    private HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("sp-hikari");
        config.addDataSourceProperty("serverName", HOST);
        config.addDataSourceProperty("port", PORT);
        config.addDataSourceProperty("databaseName", DATABASE);
        config.setJdbcUrl("jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=true");
        config.addDataSourceProperty("user", USERNAME);
        config.addDataSourceProperty("password", PASSWORD);
        config.setMaximumPoolSize(10);
        return config;
    }

    public void connect(HashSet<Biome> biomes) throws IOException, SQLException, HikariPool.PoolInitializationException {
        HikariConfig config = getHikariConfig();
        connection = new HikariDataSource(config);

        String setup;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("dbsetup.sql")) {
            setup = new BufferedReader(new InputStreamReader(in))
                    .lines()
                    .collect(Collectors.joining("\n"));

            for (Biome biome : biomes) {

                String query = setup.replace("TABLENAME", biome.name());

                try (Connection connection = this.connection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.execute();
                }
            }
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void disconnect() {
        if (isConnected()) {
            connection.close();
        }
    }

}
