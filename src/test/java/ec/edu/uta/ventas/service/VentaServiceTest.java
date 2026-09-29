package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Cliente;
import ec.edu.uta.ventas.model.Configuracion;
import ec.edu.uta.ventas.model.DetalleVenta;
import ec.edu.uta.ventas.model.Producto;
import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.model.Venta;
import ec.edu.uta.ventas.repository.ConfiguracionRepository;
import ec.edu.uta.ventas.repository.VentaRepository;
import ec.edu.uta.ventas.session.SesionActiva;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VentaServiceTest {

    private FakeVentaRepository ventaRepository;
    private FakeConfiguracionRepository configuracionRepository;

    private ConfiguracionService configuracionService;
    private SesionActiva sesionActiva;
    private VentaService ventaService;

    @BeforeEach
    void setUp() {

        ventaRepository =
                new FakeVentaRepository();

        configuracionRepository =
                new FakeConfiguracionRepository();

        /*
         * El sistema almacena el IVA como porcentaje:
         * 15 significa 15 %.
         */
        configuracionRepository.agregar(
                new Configuracion(
                        "IVA",
                        "15",
                        "Porcentaje de IVA"
                )
        );

        configuracionService =
                new ConfiguracionService(
                        configuracionRepository
                );

        sesionActiva =
                new SesionActiva();

        ventaService =
                new VentaService(
                        ventaRepository,
                        configuracionService,
                        sesionActiva
                );
    }

    /*
     * =========================
     * CREAR VENTA
     * =========================
     */

    @Test
    void debeCrearVentaConUsuarioAutenticado() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Teclado",
                        "25.50",
                        10
                );

        DetalleVenta detalle =
                new DetalleVenta(
                        producto,
                        1
                );

        Venta venta =
                ventaService.crearVenta(
                        null,
                        List.of(detalle)
                );

        assertNotNull(venta);

        assertEquals(
                1,
                venta.getId()
        );

        assertEquals(
                "FAC-000001",
                venta.getNumeroFactura()
        );

        assertEquals(
                "admin",
                venta.getUsuario().getUsername()
        );
    }

    @Test
    void debePermitirVentaConCajero() {

        iniciarSesionCajero();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "20.00",
                        10
                );

        Venta venta =
                ventaService.crearVenta(
                        null,
                        List.of(
                                new DetalleVenta(
                                        producto,
                                        1
                                )
                        )
                );

        assertNotNull(venta);

        assertEquals(
                "cajero",
                venta.getUsuario().getUsername()
        );
    }

    @Test
    void debeUsarConsumidorFinalCuandoNoHayCliente() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "20.00",
                        10
                );

        Venta venta =
                ventaService.crearVenta(
                        null,
                        List.of(
                                new DetalleVenta(
                                        producto,
                                        1
                                )
                        )
                );

        assertEquals(
                "Consumidor Final",
                venta.getCliente()
        );

        assertNull(
                venta.getClienteObj()
        );
    }

    @Test
    void debeGuardarClienteSeleccionado() {

        iniciarSesionAdmin();

        Cliente cliente =
                new Cliente();

        cliente.setId(1);
        cliente.setNombre("Juan");
        cliente.setSegundoNombre("Carlos");
        cliente.setApellido("Perez");
        cliente.setSegundoApellido("Lopez");

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "20.00",
                        10
                );

        Venta venta =
                ventaService.crearVenta(
                        cliente,
                        List.of(
                                new DetalleVenta(
                                        producto,
                                        1
                                )
                        )
                );

        assertEquals(
                cliente,
                venta.getClienteObj()
        );

        assertEquals(
                "Juan Carlos Perez Lopez",
                venta.getCliente()
        );
    }

    /*
     * =========================
     * TOTALES
     * =========================
     */

    @Test
    void debeCalcularSubtotalIvaYTotal() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Teclado",
                        "25.50",
                        10
                );

        DetalleVenta detalle =
                new DetalleVenta(
                        producto,
                        2
                );

        Venta venta =
                ventaService.crearVenta(
                        null,
                        List.of(detalle)
                );

        /*
         * 25.50 x 2 = 51.00
         *
         * IVA:
         * 51.00 x 15 / 100 = 7.65
         *
         * Total:
         * 58.65
         */

        assertEquals(
                0,
                new BigDecimal("51.00")
                        .compareTo(
                                venta.getSubtotal()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("15")
                        .compareTo(
                                venta.getPorcentajeIva()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("7.65")
                        .compareTo(
                                venta.getIva()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("58.65")
                        .compareTo(
                                venta.getTotal()
                        )
        );
    }

    @Test
    void debeCalcularVentaConVariosProductos() {

        iniciarSesionAdmin();

        Producto producto1 =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        Producto producto2 =
                crearProducto(
                        2,
                        "P002",
                        "Teclado",
                        "20.00",
                        10
                );

        Venta venta =
                ventaService.crearVenta(
                        null,
                        List.of(
                                new DetalleVenta(
                                        producto1,
                                        2
                                ),
                                new DetalleVenta(
                                        producto2,
                                        1
                                )
                        )
                );

        /*
         * 10 x 2 = 20
         * 20 x 1 = 20
         *
         * subtotal = 40
         * IVA 15% = 6
         * total = 46
         */

        assertEquals(
                0,
                new BigDecimal("40.00")
                        .compareTo(
                                venta.getSubtotal()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("6.00")
                        .compareTo(
                                venta.getIva()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("46.00")
                        .compareTo(
                                venta.getTotal()
                        )
        );
    }

    /*
     * =========================
     * VALIDACIONES
     * =========================
     */

    @Test
    void noDebeCrearVentaSinSesion() {

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        assertThrows(
                IllegalStateException.class,
                () ->
                        ventaService.crearVenta(
                                null,
                                List.of(
                                        new DetalleVenta(
                                                producto,
                                                1
                                        )
                                )
                        )
        );
    }

    @Test
    void noDebeCrearVentaSinProductos() {

        iniciarSesionAdmin();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ventaService.crearVenta(
                                null,
                                List.of()
                        )
        );
    }

    @Test
    void noDebeAceptarListaNula() {

        iniciarSesionAdmin();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ventaService.crearVenta(
                                null,
                                null
                        )
        );
    }

    @Test
    void noDebeAceptarDetalleNulo() {

        iniciarSesionAdmin();

        List<DetalleVenta> detalles =
                new ArrayList<>();

        detalles.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ventaService.crearVenta(
                                null,
                                detalles
                        )
        );
    }

    @Test
    void noDebeVenderProductoInactivo() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        producto.setActivo(false);

        DetalleVenta detalle =
                new DetalleVenta(
                        producto,
                        1
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ventaService.crearVenta(
                                null,
                                List.of(detalle)
                        )
        );
    }

    @Test
    void noDebeVenderMasDelStockDisponible() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        5
                );

        DetalleVenta detalle =
                new DetalleVenta(
                        producto,
                        6
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                ventaService.crearVenta(
                                        null,
                                        List.of(detalle)
                                )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Stock insuficiente"
                        )
        );
    }

    /*
     * =========================
     * CONSULTAS
     * =========================
     */

    @Test
    void debeBuscarVentaPorId() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        Venta guardada =
                ventaService.crearVenta(
                        null,
                        List.of(
                                new DetalleVenta(
                                        producto,
                                        1
                                )
                        )
                );

        Venta encontrada =
                ventaService.buscarPorId(
                        guardada.getId()
                );

        assertEquals(
                guardada.getId(),
                encontrada.getId()
        );
    }

    @Test
    void debeBuscarPorNumeroFactura() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        Venta guardada =
                ventaService.crearVenta(
                        null,
                        List.of(
                                new DetalleVenta(
                                        producto,
                                        1
                                )
                        )
                );

        Optional<Venta> resultado =
                ventaService
                        .buscarPorNumeroFactura(
                                guardada.getNumeroFactura()
                        );

        assertTrue(
                resultado.isPresent()
        );
    }

    @Test
    void noDebeBuscarConNumeroFacturaVacio() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ventaService
                                .buscarPorNumeroFactura(
                                        " "
                                )
        );
    }

    @Test
    void debeListarVentas() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        ventaService.crearVenta(
                null,
                List.of(
                        new DetalleVenta(
                                producto,
                                1
                        )
                )
        );

        assertEquals(
                1,
                ventaService.listar().size()
        );
    }

    @Test
    void debeBuscarVentasPorFecha() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        ventaService.crearVenta(
                null,
                List.of(
                        new DetalleVenta(
                                producto,
                                1
                        )
                )
        );

        List<Venta> resultado =
                ventaService.buscarPorFecha(
                        LocalDate.now()
                );

        assertEquals(
                1,
                resultado.size()
        );
    }

    /*
     * =========================
     * DETALLE DE VENTA
     * =========================
     */

    @Test
    void debeListarDetallesDeVenta() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Teclado",
                        "25.50",
                        10
                );

        DetalleVenta detalle =
                new DetalleVenta(
                        producto,
                        2
                );

        Venta venta =
                ventaService.crearVenta(
                        null,
                        List.of(detalle)
                );

        List<DetalleVenta> detalles =
                ventaService.listarDetalles(
                        venta.getId()
                );

        assertEquals(
                1,
                detalles.size()
        );

        assertEquals(
                "P001",
                detalles.get(0)
                        .getProducto()
                        .getCodigo()
        );

        assertEquals(
                "Teclado",
                detalles.get(0)
                        .getProducto()
                        .getNombre()
        );

        assertEquals(
                2,
                detalles.get(0)
                        .getCantidad()
        );

        assertEquals(
                0,
                new BigDecimal("25.50")
                        .compareTo(
                                detalles.get(0)
                                        .getPrecioUnitario()
                        )
        );
    }

    @Test
    void debeRetornarListaVaciaSiVentaNoExiste() {

        List<DetalleVenta> detalles =
                ventaService.listarDetalles(
                        999
                );

        assertTrue(
                detalles.isEmpty()
        );
    }

    @Test
    void noDebeListarDetallesConIdInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ventaService.listarDetalles(
                                0
                        )
        );
    }

    /*
     * =========================
     * ANULACIÓN
     * =========================
     */

    @Test
    void adminDebePoderAnularVenta() {

        iniciarSesionAdmin();

        Producto producto =
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "10.00",
                        10
                );

        Venta venta =
                ventaService.crearVenta(
                        null,
                        List.of(
                                new DetalleVenta(
                                        producto,
                                        1
                                )
                        )
                );

        assertTrue(
                ventaService.anular(
                        venta.getId()
                )
        );
    }

    @Test
    void cajeroNoDebePoderAnularVenta() {

        iniciarSesionCajero();

        assertThrows(
                IllegalStateException.class,
                () ->
                        ventaService.anular(1)
        );
    }

    @Test
    void noDebeAnularSinSesion() {

        assertThrows(
                IllegalStateException.class,
                () ->
                        ventaService.anular(1)
        );
    }

    /*
     * =========================
     * MÉTODOS AUXILIARES
     * =========================
     */

    private Producto crearProducto(
            int id,
            String codigo,
            String nombre,
            String precio,
            int stock) {

        Producto producto =
                new Producto();

        producto.setId(id);
        producto.setCodigo(codigo);
        producto.setNombre(nombre);

        producto.setPrecio(
                new BigDecimal(precio)
        );

        producto.setStock(stock);
        producto.setActivo(true);

        return producto;
    }

    private void iniciarSesionAdmin() {

        Usuario usuario =
                new Usuario();

        usuario.setId(1);
        usuario.setNombre("Administrador");
        usuario.setApellido("Sistema");
        usuario.setUsername("admin");
        usuario.setRol("ADMIN");
        usuario.setActivo(true);

        sesionActiva.iniciarSesion(
                usuario
        );
    }

    private void iniciarSesionCajero() {

        Usuario usuario =
                new Usuario();

        usuario.setId(2);
        usuario.setNombre("Usuario");
        usuario.setApellido("Cajero");
        usuario.setUsername("cajero");
        usuario.setRol("CAJERO");
        usuario.setActivo(true);

        sesionActiva.iniciarSesion(
                usuario
        );
    }

    /*
     * =========================
     * REPOSITORIO FALSO VENTAS
     * =========================
     */

    private static class FakeVentaRepository
            implements VentaRepository {

        private final List<Venta> ventas =
                new ArrayList<>();

        private int siguienteId = 1;

        @Override
        public Venta guardar(
                Venta venta) {

            venta.setId(
                    siguienteId
            );

            venta.setNumeroFactura(
                    String.format(
                            "FAC-%06d",
                            siguienteId
                    )
            );

            siguienteId++;

            ventas.add(venta);

            return venta;
        }

        @Override
        public Optional<Venta> buscarPorId(
                int id) {

            return ventas.stream()
                    .filter(
                            venta ->
                                    venta.getId()
                                            == id
                    )
                    .findFirst();
        }

        @Override
        public Optional<Venta>
        buscarPorNumeroFactura(
                String numeroFactura) {

            return ventas.stream()
                    .filter(
                            venta ->
                                    numeroFactura.equals(
                                            venta.getNumeroFactura()
                                    )
                    )
                    .findFirst();
        }

        @Override
        public List<Venta> listar() {

            return new ArrayList<>(
                    ventas
            );
        }

        @Override
        public List<Venta> buscarPorFecha(
                LocalDate fecha) {

            return ventas.stream()
                    .filter(
                            venta ->
                                    venta.getFecha()
                                            .toLocalDate()
                                            .equals(fecha)
                    )
                    .toList();
        }

        /*
         * Devuelve los detalles de la venta almacenada
         * en el repositorio falso.
         */
        @Override
        public List<DetalleVenta> listarDetalles(
                int idVenta) {

            Venta venta =
                    ventas.stream()
                            .filter(
                                    v ->
                                            v.getId()
                                                    == idVenta
                            )
                            .findFirst()
                            .orElse(null);

            if (venta == null) {
                return List.of();
            }

            return new ArrayList<>(
                    venta.getDetalles()
            );
        }

        @Override
        public boolean anular(
                int idVenta) {

            Optional<Venta> venta =
                    buscarPorId(idVenta);

            if (venta.isEmpty() ||
                    venta.get().isAnulada()) {

                return false;
            }

            venta.get().setAnulada(true);

            return true;
        }
    }

    /*
     * =========================
     * REPOSITORIO FALSO CONFIG
     * =========================
     */

    private static class FakeConfiguracionRepository
            implements ConfiguracionRepository {

        private final Map<String, Configuracion>
                configuraciones =
                new HashMap<>();

        void agregar(
                Configuracion configuracion) {

            configuraciones.put(
                    configuracion.getClave(),
                    configuracion
            );
        }

        @Override
        public List<Configuracion> listar() {

            return new ArrayList<>(
                    configuraciones.values()
            );
        }

        @Override
        public Optional<Configuracion> buscarPorClave(
                String clave) {

            return Optional.ofNullable(
                    configuraciones.get(clave)
            );
        }

        @Override
        public boolean actualizar(
                String clave,
                String valor) {

            Configuracion configuracion =
                    configuraciones.get(clave);

            if (configuracion == null) {
                return false;
            }

            configuracion.setValor(valor);

            return true;
        }
    }
}