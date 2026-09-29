package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Configuracion;
import ec.edu.uta.ventas.repository.ConfiguracionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracionServiceTest {

    private ConfiguracionRepository repository;
    private ConfiguracionService service;

    @BeforeEach
    void setUp() {

        repository =
                new ConfiguracionRepositoryFake();

        service =
                new ConfiguracionService(repository);
    }

    /*
     * =========================
     * LISTAR
     * =========================
     */

    @Test
    void debeListarConfiguraciones() {

        List<Configuracion> resultado =
                service.listar();

        assertEquals(
                5,
                resultado.size()
        );
    }

    /*
     * =========================
     * BUSCAR POR CLAVE
     * =========================
     */

    @Test
    void debeBuscarConfiguracionPorClave() {

        Configuracion configuracion =
                service.buscarPorClave("IVA");

        assertEquals(
                "IVA",
                configuracion.getClave()
        );

        assertEquals(
                "15",
                configuracion.getValor()
        );
    }

    @Test
    void debeFallarSiLaClaveNoExiste() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.buscarPorClave(
                                "NO_EXISTE"
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("No existe")
        );
    }

    @Test
    void debeFallarSiLaClaveEstaVacia() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorClave(" ")
        );
    }

    /*
     * =========================
     * IVA
     * =========================
     */

    @Test
    void debeObtenerIva() {

        BigDecimal iva =
                service.obtenerIva();

        assertEquals(
                0,
                new BigDecimal("15")
                        .compareTo(iva)
        );
    }

    @Test
    void debeActualizarIva() {

        boolean resultado =
                service.actualizarIva(
                        new BigDecimal("12")
                );

        assertTrue(resultado);

        assertEquals(
                0,
                new BigDecimal("12")
                        .compareTo(
                                service.obtenerIva()
                        )
        );
    }

    @Test
    void noDebePermitirIvaNegativo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarIva(
                        new BigDecimal("-1")
                )
        );
    }

    @Test
    void noDebePermitirIvaMayorA100() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarIva(
                        new BigDecimal("101")
                )
        );
    }

    @Test
    void noDebePermitirIvaNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarIva(null)
        );
    }

    /*
     * =========================
     * STOCK MÍNIMO
     * =========================
     */

    @Test
    void debeObtenerStockMinimo() {

        int stock =
                service.obtenerStockMinimo();

        assertEquals(
                5,
                stock
        );
    }

    @Test
    void debeActualizarStockMinimo() {

        boolean resultado =
                service.actualizarStockMinimo(10);

        assertTrue(resultado);

        assertEquals(
                10,
                service.obtenerStockMinimo()
        );
    }

    @Test
    void noDebePermitirStockMinimoNegativo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service
                        .actualizarStockMinimo(-1)
        );
    }

    /*
     * =========================
     * EMPRESA
     * =========================
     */

    @Test
    void debeObtenerDatosEmpresa() {

        assertEquals(
                "Mi Empresa",
                service.obtenerNombreEmpresa()
        );

        assertEquals(
                "1800000000001",
                service.obtenerRucEmpresa()
        );

        assertEquals(
                "Ambato",
                service.obtenerDireccionEmpresa()
        );
    }

    @Test
    void debeActualizarNombreEmpresa() {

        assertTrue(
                service.actualizarNombreEmpresa(
                        "Empresa UTA"
                )
        );

        assertEquals(
                "Empresa UTA",
                service.obtenerNombreEmpresa()
        );
    }

    @Test
    void debeActualizarRucEmpresa() {

        assertTrue(
                service.actualizarRucEmpresa(
                        "1890000000001"
                )
        );

        assertEquals(
                "1890000000001",
                service.obtenerRucEmpresa()
        );
    }

    @Test
    void noDebePermitirRucConMenosDe13Digitos() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarRucEmpresa(
                        "12345"
                )
        );
    }

    @Test
    void noDebePermitirRucConLetras() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarRucEmpresa(
                        "180000000000A"
                )
        );
    }

    @Test
    void debeActualizarDireccionEmpresa() {

        assertTrue(
                service.actualizarDireccionEmpresa(
                        "Av. Principal 123"
                )
        );

        assertEquals(
                "Av. Principal 123",
                service.obtenerDireccionEmpresa()
        );
    }

    @Test
    void noDebePermitirNombreEmpresaVacio() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service
                        .actualizarNombreEmpresa(" ")
        );
    }

    @Test
    void noDebePermitirDireccionEmpresaVacia() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service
                        .actualizarDireccionEmpresa("")
        );
    }

    /*
     * =========================
     * REPOSITORY FAKE
     * =========================
     *
     * No usamos PostgreSQL en estas
     * pruebas porque queremos probar
     * únicamente ConfiguracionService.
     */

    private static class ConfiguracionRepositoryFake
            implements ConfiguracionRepository {

        private final List<Configuracion>
                configuraciones =
                new ArrayList<>();

        ConfiguracionRepositoryFake() {

            configuraciones.add(
                    new Configuracion(
                            "IVA",
                            "15",
                            "Porcentaje de IVA"
                    )
            );

            configuraciones.add(
                    new Configuracion(
                            "STOCK_MINIMO",
                            "5",
                            "Stock mínimo"
                    )
            );

            configuraciones.add(
                    new Configuracion(
                            "EMPRESA_NOMBRE",
                            "Mi Empresa",
                            "Nombre de la empresa"
                    )
            );

            configuraciones.add(
                    new Configuracion(
                            "EMPRESA_RUC",
                            "1800000000001",
                            "RUC de la empresa"
                    )
            );

            configuraciones.add(
                    new Configuracion(
                            "EMPRESA_DIR",
                            "Ambato",
                            "Dirección de la empresa"
                    )
            );
        }

        @Override
        public List<Configuracion> listar() {

            return new ArrayList<>(
                    configuraciones
            );
        }

        @Override
        public Optional<Configuracion>
        buscarPorClave(String clave) {

            return configuraciones
                    .stream()
                    .filter(
                            configuracion ->
                                    configuracion
                                            .getClave()
                                            .equals(clave)
                    )
                    .findFirst();
        }

        @Override
        public boolean actualizar(
                String clave,
                String valor) {

            Optional<Configuracion> encontrada =
                    buscarPorClave(clave);

            if (encontrada.isEmpty()) {
                return false;
            }

            encontrada.get()
                    .setValor(valor);

            return true;
        }
    }
}