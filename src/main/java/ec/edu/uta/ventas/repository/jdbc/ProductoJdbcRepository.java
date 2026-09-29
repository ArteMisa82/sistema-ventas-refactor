package ec.edu.uta.ventas.repository.jdbc;

import ec.edu.uta.ventas.database.ConnectionProvider;
import ec.edu.uta.ventas.model.Producto;
import ec.edu.uta.ventas.repository.ProductoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoJdbcRepository implements ProductoRepository {

    private final ConnectionProvider connectionProvider;

    public ProductoJdbcRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Producto> listarActivos() {

        String sql = """
                SELECT *
                FROM productos
                WHERE activo = TRUE
                ORDER BY nombre
                """;

        return listar(sql);
    }

    @Override
    public List<Producto> listarInactivos() {

        String sql = """
                SELECT *
                FROM productos
                WHERE activo = FALSE
                ORDER BY id DESC
                """;

        return listar(sql);
    }

    @Override
    public List<Producto> buscar(String texto) {

        String sql = """
                SELECT *
                FROM productos
                WHERE activo = TRUE
                  AND (
                      codigo ILIKE ?
                      OR codigo_barras ILIKE ?
                      OR nombre ILIKE ?
                  )
                ORDER BY nombre
                """;

        List<Producto> productos = new ArrayList<>();
        String filtro = "%" + texto + "%";

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, filtro);
            statement.setString(2, filtro);
            statement.setString(3, filtro);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    productos.add(mapearProducto(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al buscar productos",
                    e
            );
        }

        return productos;
    }

    @Override
    public Optional<Producto> buscarPorId(int id) {

        String sql = """
                SELECT *
                FROM productos
                WHERE id = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapearProducto(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al buscar producto por id",
                    e
            );
        }

        return Optional.empty();
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {

        String sql = """
                SELECT *
                FROM productos
                WHERE codigo = ?
                """;

        return buscarUnoPorTexto(sql, codigo);
    }

    @Override
    public Optional<Producto> buscarPorCodigoBarras(String codigoBarras) {

        String sql = """
                SELECT *
                FROM productos
                WHERE codigo_barras = ?
                """;

        return buscarUnoPorTexto(sql, codigoBarras);
    }

    @Override
    public String obtenerSiguienteCodigo() {

        String sql = """
                SELECT COALESCE(
                    MAX(CAST(SUBSTRING(codigo FROM 2) AS INTEGER)),
                    0
                ) + 1 AS siguiente
                FROM productos
                WHERE codigo ~ '^P[0-9]+$'
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            if (resultSet.next()) {

                int siguiente = resultSet.getInt("siguiente");

                return String.format("P%03d", siguiente);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al generar el código del producto",
                    e
            );
        }

        return "P001";
    }

    @Override
    public boolean guardar(Producto producto) {

        String sql = """
                INSERT INTO productos
                    (codigo, nombre, precio, stock, codigo_barras, activo)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, producto.getCodigo());
            statement.setString(2, producto.getNombre());
            statement.setBigDecimal(3, producto.getPrecio());
            statement.setInt(4, producto.getStock());
            statement.setString(5, producto.getCodigoBarras());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al guardar producto",
                    e
            );
        }
    }

    @Override
    public boolean actualizar(Producto producto) {

        String sql = """
                UPDATE productos
                SET nombre = ?,
                    precio = ?,
                    stock = ?,
                    codigo_barras = ?
                WHERE id = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, producto.getNombre());
            statement.setBigDecimal(2, producto.getPrecio());
            statement.setInt(3, producto.getStock());
            statement.setString(4, producto.getCodigoBarras());
            statement.setInt(5, producto.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al actualizar producto",
                    e
            );
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

    private List<Producto> listar(String sql) {

        List<Producto> productos = new ArrayList<>();

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                productos.add(mapearProducto(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al listar productos",
                    e
            );
        }

        return productos;
    }

    private Optional<Producto> buscarUnoPorTexto(
            String sql,
            String valor) {

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, valor);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapearProducto(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al buscar producto",
                    e
            );
        }

        return Optional.empty();
    }

    private boolean cambiarEstado(int id, boolean activo) {

        String sql = """
                UPDATE productos
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
            throw new RuntimeException(
                    "Error al cambiar el estado del producto",
                    e
            );
        }
    }

    private Producto mapearProducto(ResultSet resultSet)
            throws SQLException {

        return new Producto(
                resultSet.getInt("id"),
                resultSet.getString("codigo"),
                resultSet.getString("nombre"),
                resultSet.getBigDecimal("precio"),
                resultSet.getInt("stock"),
                resultSet.getString("codigo_barras"),
                resultSet.getBoolean("activo")
        );
    }
}