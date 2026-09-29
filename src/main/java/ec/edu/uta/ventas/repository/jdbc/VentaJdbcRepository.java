package ec.edu.uta.ventas.repository.jdbc;

import ec.edu.uta.ventas.database.ConnectionProvider;
import ec.edu.uta.ventas.model.DetalleVenta;
import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.model.Venta;
import ec.edu.uta.ventas.repository.VentaRepository;
import ec.edu.uta.ventas.model.DetalleVenta;
import ec.edu.uta.ventas.model.Producto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaJdbcRepository
        implements VentaRepository {

    private final ConnectionProvider connectionProvider;

    public VentaJdbcRepository(
            ConnectionProvider connectionProvider) {

        this.connectionProvider =
                connectionProvider;
    }

    /*
     * =========================
     * GUARDAR VENTA
     * =========================
     */

    @Override
    public Venta guardar(Venta venta) {

        try (Connection connection =
                     connectionProvider.getConnection()) {

            connection.setAutoCommit(false);

            try {

                /*
                 * IMPORTANTE:
                 * El número de factura se obtiene usando
                 * la MISMA conexión de la transacción.
                 */
                String numeroFactura =
                        siguienteNumeroFactura(
                                connection
                        );

                venta.setNumeroFactura(
                        numeroFactura
                );

                int idVenta =
                        insertarCabecera(
                                connection,
                                venta
                        );

                venta.setId(idVenta);

                for (DetalleVenta detalle :
                        venta.getDetalles()) {

                    /*
                     * Primero verificamos y bloqueamos
                     * el producto.
                     */
                    verificarStock(
                            connection,
                            detalle.getProducto().getId(),
                            detalle.getCantidad()
                    );

                    insertarDetalle(
                            connection,
                            idVenta,
                            detalle
                    );

                    descontarStock(
                            connection,
                            detalle.getProducto().getId(),
                            detalle.getCantidad()
                    );
                }

                connection.commit();

                return venta;

            } catch (Exception e) {

                connection.rollback();

                throw new RuntimeException(
                        "Error al guardar la venta.",
                        e
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error en la transacción de venta.",
                    e
            );
        }
    }

    /*
     * =========================
     * BUSCAR POR ID
     * =========================
     */

    @Override
    public Optional<Venta> buscarPorId(
            int id) {

        String sql = """
                SELECT
                    v.id,
                    v.numero_factura,
                    v.fecha,
                    v.cliente,
                    v.subtotal,
                    v.porcentaje_iva,
                    v.iva,
                    v.total,
                    v.anulada,
                    u.id AS uid,
                    u.nombre AS unombre,
                    u.username AS uusername
                FROM ventas v
                JOIN usuarios u
                    ON u.id = v.id_usuario
                WHERE v.id = ?
                """;

        try (Connection connection =
                     connectionProvider.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    id
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return Optional.of(
                            mapearCabecera(
                                    resultSet
                            )
                    );
                }
            }

            return Optional.empty();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al buscar la venta.",
                    e
            );
        }
    }

    /*
     * =========================
     * BUSCAR POR FACTURA
     * =========================
     */

    @Override
    public Optional<Venta> buscarPorNumeroFactura(
            String numeroFactura) {

        String sql = """
                SELECT
                    v.id,
                    v.numero_factura,
                    v.fecha,
                    v.cliente,
                    v.subtotal,
                    v.porcentaje_iva,
                    v.iva,
                    v.total,
                    v.anulada,
                    u.id AS uid,
                    u.nombre AS unombre,
                    u.username AS uusername
                FROM ventas v
                JOIN usuarios u
                    ON u.id = v.id_usuario
                WHERE v.numero_factura = ?
                """;

        try (Connection connection =
                     connectionProvider.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    numeroFactura
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return Optional.of(
                            mapearCabecera(
                                    resultSet
                            )
                    );
                }
            }

            return Optional.empty();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al buscar la venta por número de factura.",
                    e
            );
        }
    }

    /*
     * =========================
     * LISTAR
     * =========================
     */

    @Override
    public List<Venta> listar() {

        String sql = """
                SELECT
                    v.id,
                    v.numero_factura,
                    v.fecha,
                    v.cliente,
                    v.subtotal,
                    v.porcentaje_iva,
                    v.iva,
                    v.total,
                    v.anulada,
                    u.id AS uid,
                    u.nombre AS unombre,
                    u.username AS uusername
                FROM ventas v
                JOIN usuarios u
                    ON u.id = v.id_usuario
                ORDER BY v.fecha DESC
                """;

        List<Venta> ventas =
                new ArrayList<>();

        try (Connection connection =
                     connectionProvider.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                ventas.add(
                        mapearCabecera(
                                resultSet
                        )
                );
            }

            return ventas;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al listar las ventas.",
                    e
            );
        }
    }

    /*
     * =========================
     * BUSCAR POR FECHA
     * =========================
     */

    @Override
    public List<Venta> buscarPorFecha(
            LocalDate fecha) {

        String sql = """
                SELECT
                    v.id,
                    v.numero_factura,
                    v.fecha,
                    v.cliente,
                    v.subtotal,
                    v.porcentaje_iva,
                    v.iva,
                    v.total,
                    v.anulada,
                    u.id AS uid,
                    u.nombre AS unombre,
                    u.username AS uusername
                FROM ventas v
                JOIN usuarios u
                    ON u.id = v.id_usuario
                WHERE DATE(v.fecha) = ?
                ORDER BY v.fecha DESC
                """;

        List<Venta> ventas =
                new ArrayList<>();

        try (Connection connection =
                     connectionProvider.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(fecha)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    ventas.add(
                            mapearCabecera(
                                    resultSet
                            )
                    );
                }
            }

            return ventas;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error al buscar ventas por fecha.",
                    e
            );
        }
    }

    @Override
public List<DetalleVenta> listarDetalles(
        int idVenta) {

    String sql = """
            SELECT
                dv.id,
                dv.id_venta,
                dv.id_producto,
                dv.cantidad,
                dv.precio_unitario,
                dv.subtotal_item,
                p.codigo,
                p.codigo_barras,
                p.nombre,
                p.precio,
                p.stock,
                p.activo
            FROM detalle_ventas dv
            JOIN productos p
                ON p.id = dv.id_producto
            WHERE dv.id_venta = ?
            ORDER BY dv.id
            """;

    List<DetalleVenta> detalles =
            new ArrayList<>();

    try (
            Connection connection =
                    connectionProvider.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
    ) {

        statement.setInt(
                1,
                idVenta
        );

        try (
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Producto producto =
                        new Producto();

                producto.setId(
                        resultSet.getInt(
                                "id_producto"
                        )
                );

                producto.setCodigo(
                        resultSet.getString(
                                "codigo"
                        )
                );

                producto.setCodigoBarras(
                        resultSet.getString(
                                "codigo_barras"
                        )
                );

                producto.setNombre(
                        resultSet.getString(
                                "nombre"
                        )
                );

                producto.setPrecio(
                        resultSet.getBigDecimal(
                                "precio"
                        )
                );

                producto.setStock(
                        resultSet.getInt(
                                "stock"
                        )
                );

                producto.setActivo(
                        resultSet.getBoolean(
                                "activo"
                        )
                );

                DetalleVenta detalle =
                        new DetalleVenta(
                                producto,
                                resultSet.getInt(
                                        "cantidad"
                                )
                        );

                detalle.setId(
                        resultSet.getInt(
                                "id"
                        )
                );

                detalle.setIdVenta(
                        resultSet.getInt(
                                "id_venta"
                        )
                );

                /*
                 * Importante:
                 * usamos el precio histórico
                 * almacenado en detalle_ventas.
                 *
                 * No debemos usar necesariamente
                 * el precio actual del producto.
                 */
                detalle.setPrecioUnitario(
                        resultSet.getBigDecimal(
                                "precio_unitario"
                        )
                );

                detalles.add(
                        detalle
                );
            }
        }

        return detalles;

    } catch (SQLException e) {

        throw new RuntimeException(
                "Error al consultar el detalle de la venta.",
                e
        );
    }
}

    /*
     * =========================
     * ANULAR VENTA
     * =========================
     */

    @Override
    public boolean anular(
            int idVenta) {

        try (Connection connection =
                     connectionProvider.getConnection()) {

            connection.setAutoCommit(false);

            try {

                if (!bloquearVentaParaAnular(
                        connection,
                        idVenta)) {

                    connection.rollback();

                    return false;
                }

                devolverStock(
                        connection,
                        idVenta
                );

                marcarComoAnulada(
                        connection,
                        idVenta
                );

                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw new RuntimeException(
                        "Error al anular la venta.",
                        e
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error en la transacción de anulación.",
                    e
            );
        }
    }

    /*
     * =========================
     * NÚMERO DE FACTURA
     * =========================
     */

    private String siguienteNumeroFactura(
            Connection connection)
            throws SQLException {

        String sql =
                "SELECT siguiente_numero_factura()";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            if (!resultSet.next()) {

                throw new SQLException(
                        "No se pudo generar el número de factura."
                );
            }

            String numero =
                    resultSet.getString(1);

            if (numero == null ||
                    numero.isBlank()) {

                throw new SQLException(
                        "El número de factura generado no es válido."
                );
            }

            return numero;
        }
    }

    /*
     * =========================
     * INSERTAR CABECERA
     * =========================
     */

    private int insertarCabecera(
            Connection connection,
            Venta venta)
            throws SQLException {

        String sql = """
                INSERT INTO ventas (
                    numero_factura,
                    fecha,
                    cliente,
                    id_usuario,
                    id_cliente,
                    subtotal,
                    porcentaje_iva,
                    iva,
                    total
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    venta.getNumeroFactura()
            );

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            venta.getFecha()
                    )
            );

            statement.setString(
                    3,
                    venta.getCliente()
            );

            statement.setInt(
                    4,
                    venta.getUsuario().getId()
            );

            if (venta.getClienteObj() != null) {

                statement.setInt(
                        5,
                        venta.getClienteObj().getId()
                );

            } else {

                statement.setNull(
                        5,
                        Types.INTEGER
                );
            }

            statement.setBigDecimal(
                    6,
                    venta.getSubtotal()
            );

            statement.setBigDecimal(
                    7,
                    venta.getPorcentajeIva()
            );

            statement.setBigDecimal(
                    8,
                    venta.getIva()
            );

            statement.setBigDecimal(
                    9,
                    venta.getTotal()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {

                    throw new SQLException(
                            "No se pudo registrar la cabecera de la venta."
                    );
                }

                return resultSet.getInt(
                        "id"
                );
            }
        }
    }

    /*
     * =========================
     * INSERTAR DETALLE
     * =========================
     */

    private void insertarDetalle(
            Connection connection,
            int idVenta,
            DetalleVenta detalle)
            throws SQLException {

        String sql = """
                INSERT INTO detalle_ventas (
                    id_venta,
                    id_producto,
                    cantidad,
                    precio_unitario,
                    subtotal_item
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    idVenta
            );

            statement.setInt(
                    2,
                    detalle.getProducto().getId()
            );

            statement.setInt(
                    3,
                    detalle.getCantidad()
            );

            statement.setBigDecimal(
                    4,
                    detalle.getPrecioUnitario()
            );

            statement.setBigDecimal(
                    5,
                    detalle.getSubtotalItem()
            );

            statement.executeUpdate();
        }
    }

    /*
     * =========================
     * VERIFICAR STOCK
     * =========================
     */

    private void verificarStock(
            Connection connection,
            int idProducto,
            int cantidad)
            throws SQLException {

        String sql = """
                SELECT nombre, stock
                FROM productos
                WHERE id = ?
                  AND activo = TRUE
                FOR UPDATE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    idProducto
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {

                    throw new SQLException(
                            "El producto no existe o está inactivo."
                    );
                }

                String nombre =
                        resultSet.getString(
                                "nombre"
                        );

                int stock =
                        resultSet.getInt(
                                "stock"
                        );

                if (stock < cantidad) {

                    throw new SQLException(
                            "Stock insuficiente para "
                                    + nombre
                                    + ". Disponible: "
                                    + stock
                    );
                }
            }
        }
    }

    /*
     * =========================
     * DESCONTAR STOCK
     * =========================
     */

    private void descontarStock(
            Connection connection,
            int idProducto,
            int cantidad)
            throws SQLException {

        String sql = """
                UPDATE productos
                SET stock = stock - ?
                WHERE id = ?
                  AND stock >= ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    cantidad
            );

            statement.setInt(
                    2,
                    idProducto
            );

            statement.setInt(
                    3,
                    cantidad
            );

            int filas =
                    statement.executeUpdate();

            if (filas != 1) {

                throw new SQLException(
                        "No se pudo descontar el stock del producto ID: "
                                + idProducto
                );
            }
        }
    }

    /*
     * =========================
     * ANULACIÓN
     * =========================
     */

    private boolean bloquearVentaParaAnular(
            Connection connection,
            int idVenta)
            throws SQLException {

        String sql = """
                SELECT anulada
                FROM ventas
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    idVenta
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {

                    return false;
                }

                return !resultSet.getBoolean(
                        "anulada"
                );
            }
        }
    }

    private void devolverStock(
            Connection connection,
            int idVenta)
            throws SQLException {

        String sql = """
                SELECT id_producto, cantidad
                FROM detalle_ventas
                WHERE id_venta = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    idVenta
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    int idProducto =
                            resultSet.getInt(
                                    "id_producto"
                            );

                    int cantidad =
                            resultSet.getInt(
                                    "cantidad"
                            );

                    devolverStockProducto(
                            connection,
                            idProducto,
                            cantidad
                    );
                }
            }
        }
    }

    private void devolverStockProducto(
            Connection connection,
            int idProducto,
            int cantidad)
            throws SQLException {

        String sql = """
                UPDATE productos
                SET stock = stock + ?
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    cantidad
            );

            statement.setInt(
                    2,
                    idProducto
            );

            int filas =
                    statement.executeUpdate();

            if (filas != 1) {

                throw new SQLException(
                        "No se pudo devolver el stock del producto ID: "
                                + idProducto
                );
            }
        }
    }

    private void marcarComoAnulada(
            Connection connection,
            int idVenta)
            throws SQLException {

        String sql = """
                UPDATE ventas
                SET anulada = TRUE
                WHERE id = ?
                  AND anulada = FALSE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    idVenta
            );

            int filas =
                    statement.executeUpdate();

            if (filas != 1) {

                throw new SQLException(
                        "No se pudo anular la venta."
                );
            }
        }
    }

    /*
     * =========================
     * MAPEO
     * =========================
     */

    private Venta mapearCabecera(
            ResultSet resultSet)
            throws SQLException {

        Usuario usuario =
                new Usuario();

        usuario.setId(
                resultSet.getInt(
                        "uid"
                )
        );

        usuario.setNombre(
                resultSet.getString(
                        "unombre"
                )
        );

        usuario.setUsername(
                resultSet.getString(
                        "uusername"
                )
        );

        Venta venta =
                new Venta();

        venta.setId(
                resultSet.getInt(
                        "id"
                )
        );

        venta.setNumeroFactura(
                resultSet.getString(
                        "numero_factura"
                )
        );

        venta.setFecha(
                resultSet
                        .getTimestamp("fecha")
                        .toLocalDateTime()
        );

        venta.setCliente(
                resultSet.getString(
                        "cliente"
                )
        );

        venta.setUsuario(
                usuario
        );

        venta.setSubtotal(
                resultSet.getBigDecimal(
                        "subtotal"
                )
        );

        venta.setPorcentajeIva(
                resultSet.getBigDecimal(
                        "porcentaje_iva"
                )
        );

        venta.setIva(
                resultSet.getBigDecimal(
                        "iva"
                )
        );

        venta.setTotal(
                resultSet.getBigDecimal(
                        "total"
                )
        );

        venta.setAnulada(
                resultSet.getBoolean(
                        "anulada"
                )
        );

        return venta;
    }
}