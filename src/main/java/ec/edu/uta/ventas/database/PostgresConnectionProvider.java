package ec.edu.uta.ventas.database;

import ec.edu.uta.ventas.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Implementación de ConnectionProvider utilizando PostgreSQL.
 */

public class PostgresConnectionProvider implements ConnectionProvider {

     private final DatabaseConfig databaseConfig;

    public PostgresConnectionProvider(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                databaseConfig.getUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
        );
    }
    
}
