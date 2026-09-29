package ec.edu.uta.ventas.session;

import ec.edu.uta.ventas.model.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SesionActivaTest {

    private SesionActiva sesionActiva;

    @BeforeEach
    void setUp() {
        sesionActiva =
                new SesionActiva();
    }

    @Test
    void inicialmenteNoDebeExistirSesion() {

        assertFalse(
                sesionActiva.haySesionActiva()
        );
    }

    @Test
    void debeIniciarSesion() {

        Usuario usuario =
                crearUsuario(
                        "cperez",
                        "CAJERO",
                        true
                );

        boolean resultado =
                sesionActiva.iniciarSesion(
                        usuario
                );

        assertTrue(resultado);

        assertTrue(
                sesionActiva.haySesionActiva()
        );

        assertEquals(
                usuario,
                sesionActiva.getUsuarioActual()
        );
    }

    @Test
    void debeReconocerAdministrador() {

        Usuario usuario =
                crearUsuario(
                        "admin",
                        "ADMIN",
                        true
                );

        sesionActiva.iniciarSesion(
                usuario
        );

        assertTrue(
                sesionActiva.esAdmin()
        );

        assertFalse(
                sesionActiva.esCajero()
        );
    }

    @Test
    void debeReconocerCajero() {

        Usuario usuario =
                crearUsuario(
                        "cperez",
                        "CAJERO",
                        true
                );

        sesionActiva.iniciarSesion(
                usuario
        );

        assertTrue(
                sesionActiva.esCajero()
        );

        assertFalse(
                sesionActiva.esAdmin()
        );
    }

    @Test
    void noDebeAceptarUsuarioInactivo() {

        Usuario usuario =
                crearUsuario(
                        "cperez",
                        "CAJERO",
                        false
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                sesionActiva.iniciarSesion(
                                        usuario
                                )
                );

        assertEquals(
                "El usuario se encuentra inactivo.",
                exception.getMessage()
        );

        assertFalse(
                sesionActiva.haySesionActiva()
        );
    }

    @Test
    void debeCerrarSesion() {

        Usuario usuario =
                crearUsuario(
                        "cperez",
                        "CAJERO",
                        true
                );

        sesionActiva.iniciarSesion(
                usuario
        );

        assertTrue(
                sesionActiva.haySesionActiva()
        );

        sesionActiva.cerrarSesion();

        assertFalse(
                sesionActiva.haySesionActiva()
        );
    }

    @Test
    void debeFallarAlObtenerUsuarioSinSesion() {

        assertThrows(
                IllegalStateException.class,
                () ->
                        sesionActiva
                                .getUsuarioActual()
        );
    }

    private Usuario crearUsuario(
            String username,
            String rol,
            boolean activo) {

        return new Usuario(
                1,
                "Usuario",
                "Prueba",
                username,
                null,
                rol,
                activo
        );
    }
}