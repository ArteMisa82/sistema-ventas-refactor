package ec.edu.uta.ventas.database;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionProvider {

    Connection getConnection() throws SQLException;
}
