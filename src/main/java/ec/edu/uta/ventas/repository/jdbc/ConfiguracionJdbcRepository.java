package ec.edu.uta.ventas.repository.jdbc;

import ec.edu.uta.ventas.database.ConnectionProvider;
import ec.edu.uta.ventas.model.Configuracion;
import ec.edu.uta.ventas.repository.ConfiguracionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfiguracionJdbcRepository
        implements ConfiguracionRepository {

    private final ConnectionProvider connectionProvider;

    public ConfiguracionJdbcRepository(
            ConnectionProvider connectionProvider) {

        this.connectionProvider =
                connectionProvider;
    }

    @Override
    public List<Configuracion> listar() {

        String sql = """
                SELECT clave, valor, descripcion
                FROM configuracion
                ORDER BY clave
                """;

        List<Configuracion> configuraciones =
                new ArrayList<>();

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                configuraciones.add(
                        mapear(resultSet)
                );
            }

            return configuraciones;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al listar las configuraciones.",
                    e
            );
        }
    }

    @Override
    public Optional<Configuracion> buscarPorClave(
            String clave) {

        String sql = """
                SELECT clave, valor, descripcion
                FROM configuracion
                WHERE clave = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    clave
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    return Optional.of(
                            mapear(resultSet)
                    );
                }
            }

            return Optional.empty();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al buscar la configuración: "
                            + clave,
                    e
            );
        }
    }

    @Override
    public boolean actualizar(
            String clave,
            String valor) {

        String sql = """
                UPDATE configuracion
                SET valor = ?
                WHERE clave = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    valor
            );

            statement.setString(
                    2,
                    clave
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al actualizar la configuración: "
                            + clave,
                    e
            );
        }
    }

    private Configuracion mapear(
            ResultSet resultSet)
            throws SQLException {

        return new Configuracion(
                resultSet.getString("clave"),
                resultSet.getString("valor"),
                resultSet.getString("descripcion")
        );
    }
}