package ec.edu.uta.ventas.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Sha256PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {

        passwordEncoder =
                new Sha256PasswordEncoder();
    }

    @Test
    void debeGenerarHashDePassword() {

        String resultado =
                passwordEncoder.encode("123456");

        assertNotNull(resultado);

        assertNotEquals(
                "123456",
                resultado
        );

        assertEquals(
                64,
                resultado.length()
        );
    }

    @Test
    void debeGenerarSiempreElMismoHash() {

        String primerHash =
                passwordEncoder.encode("123456");

        String segundoHash =
                passwordEncoder.encode("123456");

        assertEquals(
                primerHash,
                segundoHash
        );
    }

    @Test
    void passwordsDiferentesDebenGenerarHashesDiferentes() {

        String primerHash =
                passwordEncoder.encode("123456");

        String segundoHash =
                passwordEncoder.encode("abcdef");

        assertNotEquals(
                primerHash,
                segundoHash
        );
    }

    @Test
    void debeVerificarPasswordCorrecto() {

        String hash =
                passwordEncoder.encode("123456");

        assertTrue(
                passwordEncoder.matches(
                        "123456",
                        hash
                )
        );
    }

    @Test
    void noDebeAceptarPasswordIncorrecto() {

        String hash =
                passwordEncoder.encode("123456");

        assertFalse(
                passwordEncoder.matches(
                        "incorrecta",
                        hash
                )
        );
    }

    @Test
    void noDebeAceptarValoresNulosEnMatches() {

        assertFalse(
                passwordEncoder.matches(
                        null,
                        "hash"
                )
        );

        assertFalse(
                passwordEncoder.matches(
                        "123456",
                        null
                )
        );
    }

    @Test
    void debeRechazarPasswordNuloAlCodificar() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordEncoder.encode(null)
                );

        assertEquals(
                "La contraseña no puede ser nula",
                exception.getMessage()
        );
    }
}