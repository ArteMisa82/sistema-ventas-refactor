package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Producto;
import ec.edu.uta.ventas.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductoServiceTest {

    private ProductoRepositoryFalso repository;
    private ProductoService service;

    @BeforeEach
    void setUp() {
        repository = new ProductoRepositoryFalso();
        service = new ProductoService(repository);
    }

    @Test
    void debeRegistrarProductoValido() {

        boolean resultado = service.registrar(
                "Mouse Inalámbrico",
                new BigDecimal("18.50"),
                25,
                "786100000001"
        );

        assertTrue(resultado);
        assertEquals(1, repository.productos.size());

        Producto producto = repository.productos.get(0);

        assertEquals("P001", producto.getCodigo());
        assertEquals(
                "Mouse Inalámbrico",
                producto.getNombre()
        );
        assertEquals(
                new BigDecimal("18.50"),
                producto.getPrecio()
        );
        assertEquals(25, producto.getStock());
        assertEquals(
                "786100000001",
                producto.getCodigoBarras()
        );
        assertTrue(producto.isActivo());
    }

    @Test
    void noDebeRegistrarNombreVacio() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.registrar(
                                "",
                                new BigDecimal("10.00"),
                                5,
                                "111"
                        )
                );

        assertEquals(
                "El nombre es obligatorio.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeRegistrarPrecioNulo() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.registrar(
                                "Mouse",
                                null,
                                5,
                                "111"
                        )
                );

        assertEquals(
                "El precio es obligatorio.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeRegistrarPrecioCero() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.registrar(
                                "Mouse",
                                BigDecimal.ZERO,
                                5,
                                "111"
                        )
                );

        assertEquals(
                "El precio debe ser mayor que cero.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeRegistrarPrecioNegativo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrar(
                        "Mouse",
                        new BigDecimal("-10"),
                        5,
                        "111"
                )
        );
    }

    @Test
    void noDebeRegistrarStockNegativo() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.registrar(
                                "Mouse",
                                new BigDecimal("10"),
                                -1,
                                "111"
                        )
                );

        assertEquals(
                "El stock no puede ser negativo.",
                exception.getMessage()
        );
    }

    @Test
    void debePermitirStockCero() {

        boolean resultado =
                service.registrar(
                        "Mouse",
                        new BigDecimal("10"),
                        0,
                        "111"
                );

        assertTrue(resultado);
    }

    @Test
    void noDebeRegistrarCodigoBarrasDuplicado() {

        Producto existente = crearProducto(
                1,
                "P001",
                "Mouse",
                "786100000001",
                true
        );

        repository.productos.add(existente);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.registrar(
                                "Teclado",
                                new BigDecimal("25"),
                                10,
                                "786100000001"
                        )
                );

        assertEquals(
                "El código de barras ya está registrado.",
                exception.getMessage()
        );
    }

    @Test
    void debePermitirCodigoBarrasVacio() {

        boolean resultado =
                service.registrar(
                        "Producto sin código de barras",
                        new BigDecimal("15"),
                        5,
                        ""
                );

        assertTrue(resultado);

        Producto producto =
                repository.productos.get(0);

        assertNull(producto.getCodigoBarras());
    }

    @Test
    void debeActualizarProductoValido() {

        Producto producto = crearProducto(
                1,
                "P001",
                "Mouse",
                "111",
                true
        );

        repository.productos.add(producto);

        boolean resultado =
                service.actualizar(
                        1,
                        "Mouse Gamer",
                        new BigDecimal("30"),
                        20,
                        "111"
                );

        assertTrue(resultado);

        Producto actualizado =
                repository.buscarPorId(1).orElseThrow();

        assertEquals(
                "Mouse Gamer",
                actualizado.getNombre()
        );

        assertEquals(
                new BigDecimal("30"),
                actualizado.getPrecio()
        );

        assertEquals(
                20,
                actualizado.getStock()
        );
    }

    @Test
    void debePermitirActualizarConSuMismoCodigoBarras() {

        Producto producto = crearProducto(
                1,
                "P001",
                "Mouse",
                "111",
                true
        );

        repository.productos.add(producto);

        assertDoesNotThrow(
                () -> service.actualizar(
                        1,
                        "Mouse actualizado",
                        new BigDecimal("20"),
                        10,
                        "111"
                )
        );
    }

    @Test
    void noDebeActualizarConCodigoBarrasDeOtroProducto() {

        repository.productos.add(
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "111",
                        true
                )
        );

        repository.productos.add(
                crearProducto(
                        2,
                        "P002",
                        "Teclado",
                        "222",
                        true
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.actualizar(
                                1,
                                "Mouse",
                                new BigDecimal("20"),
                                10,
                                "222"
                        )
                );

        assertEquals(
                "El código de barras ya está registrado.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeActualizarIdInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizar(
                        0,
                        "Mouse",
                        new BigDecimal("20"),
                        10,
                        "111"
                )
        );
    }

    @Test
    void noDebeActualizarProductoInexistente() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.actualizar(
                                999,
                                "Mouse",
                                new BigDecimal("20"),
                                10,
                                "111"
                        )
                );

        assertEquals(
                "El producto no existe.",
                exception.getMessage()
        );
    }

    @Test
    void debeDesactivarProductoActivo() {

        repository.productos.add(
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "111",
                        true
                )
        );

        boolean resultado =
                service.desactivar(1);

        assertTrue(resultado);

        Producto producto =
                repository.buscarPorId(1).orElseThrow();

        assertFalse(producto.isActivo());
    }

    @Test
    void noDebeDesactivarProductoYaInactivo() {

        repository.productos.add(
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "111",
                        false
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.desactivar(1)
                );

        assertEquals(
                "El producto ya se encuentra inactivo.",
                exception.getMessage()
        );
    }

    @Test
    void debeReactivarProductoInactivo() {

        repository.productos.add(
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "111",
                        false
                )
        );

        boolean resultado =
                service.reactivar(1);

        assertTrue(resultado);

        Producto producto =
                repository.buscarPorId(1).orElseThrow();

        assertTrue(producto.isActivo());
    }

    @Test
    void noDebeReactivarProductoYaActivo() {

        repository.productos.add(
                crearProducto(
                        1,
                        "P001",
                        "Mouse",
                        "111",
                        true
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.reactivar(1)
                );

        assertEquals(
                "El producto ya se encuentra activo.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeAceptarIdInvalidoAlDesactivar() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.desactivar(0)
        );
    }

    @Test
    void noDebeAceptarIdInvalidoAlReactivar() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.reactivar(-1)
        );
    }

    private Producto crearProducto(
            int id,
            String codigo,
            String nombre,
            String codigoBarras,
            boolean activo) {

        return new Producto(
                id,
                codigo,
                nombre,
                new BigDecimal("10.00"),
                10,
                codigoBarras,
                activo
        );
    }

    /*
     * Repositorio falso utilizado únicamente
     * para probar ProductoService sin PostgreSQL.
     */
    private static class ProductoRepositoryFalso
            implements ProductoRepository {

        private final List<Producto> productos =
                new ArrayList<>();

        @Override
        public List<Producto> listarActivos() {

            return productos.stream()
                    .filter(Producto::isActivo)
                    .toList();
        }

        @Override
        public List<Producto> listarInactivos() {

            return productos.stream()
                    .filter(p -> !p.isActivo())
                    .toList();
        }

        @Override
        public List<Producto> buscar(String texto) {

            String filtro =
                    texto.toLowerCase();

            return productos.stream()
                    .filter(Producto::isActivo)
                    .filter(p ->
                            contiene(
                                    p.getCodigo(),
                                    filtro)
                            ||
                            contiene(
                                    p.getCodigoBarras(),
                                    filtro)
                            ||
                            contiene(
                                    p.getNombre(),
                                    filtro))
                    .toList();
        }

        @Override
        public Optional<Producto> buscarPorId(int id) {

            return productos.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst();
        }

        @Override
        public Optional<Producto> buscarPorCodigo(
                String codigo) {

            return productos.stream()
                    .filter(p ->
                            codigo.equals(
                                    p.getCodigo()))
                    .findFirst();
        }

        @Override
        public Optional<Producto> buscarPorCodigoBarras(
                String codigoBarras) {

            return productos.stream()
                    .filter(p ->
                            codigoBarras.equals(
                                    p.getCodigoBarras()))
                    .findFirst();
        }

        @Override
        public String obtenerSiguienteCodigo() {

            int siguiente =
                    productos.size() + 1;

            return String.format(
                    "P%03d",
                    siguiente);
        }

        @Override
        public boolean guardar(
                Producto producto) {

            producto.setId(
                    productos.size() + 1);

            producto.setActivo(true);

            productos.add(producto);

            return true;
        }

        @Override
        public boolean actualizar(
                Producto producto) {

            return buscarPorId(
                    producto.getId())
                    .isPresent();
        }

        @Override
        public boolean desactivar(int id) {

            Optional<Producto> producto =
                    buscarPorId(id);

            if (producto.isEmpty()) {
                return false;
            }

            producto.get().setActivo(false);

            return true;
        }

        @Override
        public boolean reactivar(int id) {

            Optional<Producto> producto =
                    buscarPorId(id);

            if (producto.isEmpty()) {
                return false;
            }

            producto.get().setActivo(true);

            return true;
        }

        private static boolean contiene(
                String valor,
                String filtro) {

            return valor != null &&
                    valor.toLowerCase()
                            .contains(filtro);
        }
    }
}