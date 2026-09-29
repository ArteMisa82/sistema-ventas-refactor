package ec.edu.uta.ventas.repository.jdbc;

import ec.edu.uta.ventas.database.ConnectionProvider;
import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.repository.UsuarioRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioJdbcRepository implements UsuarioRepository {

    private final ConnectionProvider connectionProvider;

    public UsuarioJdbcRepository(
            ConnectionProvider connectionProvider) {

        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Usuario> listarTodos() {

        String sql = """
                SELECT
                    u.id,
                    u.nombre,
                    u.apellido,
                    u.username,
                    u.activo,
                    r.nombre AS rol
                FROM usuarios u
                JOIN roles r ON r.id = u.id_rol
                ORDER BY u.nombre
                """;

        return listar(sql);
    }

    @Override
    public List<Usuario> buscar(String texto) {

        String sql = """
                SELECT
                    u.id,
                    u.nombre,
                    u.apellido,
                    u.username,
                    u.activo,
                    r.nombre AS rol
                FROM usuarios u
                JOIN roles r ON r.id = u.id_rol
                WHERE LOWER(u.nombre) LIKE ?
                   OR LOWER(u.apellido) LIKE ?
                   OR LOWER(u.username) LIKE ?
                ORDER BY u.nombre
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            String filtro =
                    "%" + texto.toLowerCase() + "%";

            statement.setString(1, filtro);
            statement.setString(2, filtro);
            statement.setString(3, filtro);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    usuarios.add(
                            mapearUsuario(resultSet)
                    );
                }
            }

            return usuarios;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error buscando usuarios.",
                    e
            );
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {

        String sql = """
                SELECT
                    u.id,
                    u.nombre,
                    u.apellido,
                    u.username,
                    u.activo,
                    r.nombre AS rol
                FROM usuarios u
                JOIN roles r ON r.id = u.id_rol
                WHERE u.id = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return Optional.of(
                            mapearUsuario(resultSet)
                    );
                }
            }

            return Optional.empty();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error buscando usuario.",
                    e
            );
        }
    }

    @Override
    public boolean existeUsername(
            String username,
            int idExcluir) {

        String sql = """
                SELECT 1
                FROM usuarios
                WHERE username = ?
                  AND id <> ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);
            statement.setInt(2, idExcluir);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error verificando username.",
                    e
            );
        }
    }

    @Override
    public boolean guardar(
            Usuario usuario,
            String passwordHash) {

        String sql = """
                INSERT INTO usuarios
                    (nombre, apellido, username, password, id_rol)
                VALUES
                    (?, ?, ?, ?,
                     (SELECT id
                      FROM roles
                      WHERE nombre = ?))
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    usuario.getNombre()
            );

            statement.setString(
                    2,
                    usuario.getApellido()
            );

            statement.setString(
                    3,
                    usuario.getUsername()
            );

            statement.setString(
                    4,
                    passwordHash
            );

            statement.setString(
                    5,
                    usuario.getRol()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al guardar el usuario.",
                    e
            );
        }
    }

    @Override
    public boolean actualizar(Usuario usuario) {

        String sql = """
                UPDATE usuarios
                SET nombre = ?,
                    apellido = ?,
                    username = ?,
                    activo = ?,
                    id_rol = (
                        SELECT id
                        FROM roles
                        WHERE nombre = ?
                    )
                WHERE id = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    usuario.getNombre()
            );

            statement.setString(
                    2,
                    usuario.getApellido()
            );

            statement.setString(
                    3,
                    usuario.getUsername()
            );

            statement.setBoolean(
                    4,
                    usuario.isActivo()
            );

            statement.setString(
                    5,
                    usuario.getRol()
            );

            statement.setInt(
                    6,
                    usuario.getId()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al actualizar usuario.",
                    e
            );
        }
    }

    @Override
    public boolean desactivar(int id) {

        String sql = """
                UPDATE usuarios
                SET activo = FALSE
                WHERE id = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al desactivar el usuario.",
                    e
            );
        }
    }

    @Override
    public boolean cambiarPassword(
            int id,
            String passwordHash) {

        String sql = """
                UPDATE usuarios
                SET password = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    passwordHash
            );

            statement.setInt(
                    2,
                    id
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al actualizar la contraseña.",
                    e
            );
        }
    }

    private List<Usuario> listar(String sql) {

        List<Usuario> usuarios =
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

                usuarios.add(
                        mapearUsuario(resultSet)
                );
            }

            return usuarios;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error listando usuarios.",
                    e
            );
        }
    }

    private Usuario mapearUsuario(ResultSet resultSet)
            throws SQLException {

        Usuario usuario = new Usuario();

        usuario.setId(
                resultSet.getInt("id")
        );

        usuario.setNombre(
                resultSet.getString("nombre")
        );

        usuario.setApellido(
                resultSet.getString("apellido")
        );

        usuario.setUsername(
                resultSet.getString("username")
        );

        usuario.setRol(
                resultSet.getString("rol")
        );

        usuario.setActivo(
                resultSet.getBoolean("activo")
        );

        return usuario;
    }

    @Override
        public Optional<Usuario> autenticar(
                String username,
                String passwordHash) {

        String sql = """
                SELECT
                        u.id,
                        u.nombre,
                        u.apellido,
                        u.username,
                        u.activo,
                        r.nombre AS rol
                FROM usuarios u
                JOIN roles r
                        ON r.id = u.id_rol
                WHERE u.username = ?
                AND u.password = ?
                AND u.activo = TRUE
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

                statement.setString(
                        1,
                        username
                );

                statement.setString(
                        2,
                        passwordHash
                );

                try (ResultSet resultSet =
                        statement.executeQuery()) {

                if (resultSet.next()) {
                        return Optional.of(
                                mapearUsuario(resultSet)
                        );
                }

                return Optional.empty();
                }

        } catch (SQLException e) {

                throw new RuntimeException(
                        "Error al autenticar el usuario.",
                        e
                );
        }
        }
}