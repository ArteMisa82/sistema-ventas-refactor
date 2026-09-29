package ec.edu.uta.ventas.repository.jdbc;

import ec.edu.uta.ventas.database.ConnectionProvider;
import ec.edu.uta.ventas.model.Cliente;
import ec.edu.uta.ventas.repository.ClienteRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteJdbcRepository  implements ClienteRepository{
    
    private final ConnectionProvider connectionProvider;

    public ClienteJdbcRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Cliente> listarActivos() {

        String sql = "SELECT * FROM clientes WHERE activo = TRUE ORDER BY nombre";

        return listar(sql);
    }

    @Override
    public List<Cliente> listarInactivos() {

        String sql = """
                SELECT * FROM clientes
                WHERE activo = FALSE
                ORDER BY id DESC
                """;

        return listar(sql);
    }

    @Override
    public List<Cliente> buscar(String texto) {

        String sql = """
                SELECT * FROM clientes
                WHERE activo = TRUE
                  AND (
                      LOWER(nombre) LIKE ?
                      OR LOWER(apellido) LIKE ?
                      OR cedula LIKE ?
                  )
                ORDER BY nombre
                """;

        List<Cliente> clientes = new ArrayList<>();

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            String filtro = "%" + texto.toLowerCase() + "%";

            statement.setString(1, filtro);
            statement.setString(2, filtro);
            statement.setString(3, filtro);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    clientes.add(mapearCliente(resultSet));
                }
            }

            return clientes;

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar clientes.", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorCedula(String cedula) {

        String sql = """
                SELECT * FROM clientes
                WHERE cedula = ?
                  AND activo = TRUE
                """;

        return buscarPorCedulaYEstado(sql, cedula);
    }

    @Override
    public Optional<Cliente> buscarInactivoPorCedula(String cedula) {

        String sql = """
                SELECT * FROM clientes
                WHERE cedula = ?
                  AND activo = FALSE
                """;

        return buscarPorCedulaYEstado(sql, cedula);
    }

    @Override
    public boolean existeCedula(String cedula, int idExcluir) {

        String sql = """
                SELECT 1 FROM clientes
                WHERE cedula = ?
                  AND id <> ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, cedula);
            statement.setInt(2, idExcluir);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar la cédula.", e);
        }
    }

    @Override
    public boolean guardar(Cliente cliente) {

        String sql = """
                INSERT INTO clientes (
                    cedula,
                    nombre,
                    apellido,
                    segundo_nombre,
                    segundo_apellido,
                    direccion,
                    telefono,
                    email
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            asignarDatosCliente(statement, cliente);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el cliente.", e);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {

        String sql = """
                UPDATE clientes
                SET cedula = ?,
                    nombre = ?,
                    apellido = ?,
                    segundo_nombre = ?,
                    segundo_apellido = ?,
                    direccion = ?,
                    telefono = ?,
                    email = ?
                WHERE id = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            asignarDatosCliente(statement, cliente);
            statement.setInt(9, cliente.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el cliente.", e);
        }
    }

    @Override
    public boolean desactivar(int id) {
        return cambiarEstado(id, false);
    }

    @Override
    public boolean reactivar(int id) {
        return cambiarEstado(id, true);
    }

    private boolean cambiarEstado(int id, boolean activo) {

        String sql = """
                UPDATE clientes
                SET activo = ?
                WHERE id = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setBoolean(1, activo);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar el estado del cliente.", e);
        }
    }

    private Optional<Cliente> buscarPorCedulaYEstado(
            String sql,
            String cedula) {

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, cedula);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapearCliente(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el cliente.", e);
        }
    }

    private List<Cliente> listar(String sql) {

        List<Cliente> clientes = new ArrayList<>();

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                clientes.add(mapearCliente(resultSet));
            }

            return clientes;

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes.", e);
        }
    }

    private void asignarDatosCliente(
            PreparedStatement statement,
            Cliente cliente) throws SQLException {

        statement.setString(1, cliente.getCedula());
        statement.setString(2, cliente.getNombre());
        statement.setString(3, cliente.getApellido());
        statement.setString(4, cliente.getSegundoNombre());
        statement.setString(5, cliente.getSegundoApellido());
        statement.setString(6, cliente.getDireccion());
        statement.setString(7, cliente.getTelefono());
        statement.setString(8, cliente.getEmail());
    }

    private Cliente mapearCliente(ResultSet resultSet)
            throws SQLException {

        Cliente cliente = new Cliente();

        cliente.setId(resultSet.getInt("id"));
        cliente.setCedula(resultSet.getString("cedula"));
        cliente.setNombre(resultSet.getString("nombre"));
        cliente.setApellido(resultSet.getString("apellido"));
        cliente.setSegundoNombre(
                resultSet.getString("segundo_nombre"));
        cliente.setSegundoApellido(
                resultSet.getString("segundo_apellido"));
        cliente.setDireccion(
                resultSet.getString("direccion"));
        cliente.setTelefono(
                resultSet.getString("telefono"));
        cliente.setEmail(
                resultSet.getString("email"));
        cliente.setActivo(
                resultSet.getBoolean("activo"));

        return cliente;
    }
}
