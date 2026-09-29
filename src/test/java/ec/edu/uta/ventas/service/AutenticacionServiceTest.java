package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.repository.UsuarioRepository;
import ec.edu.uta.ventas.security.PasswordEncoder;
import ec.edu.uta.ventas.security.Sha256PasswordEncoder;
import ec.edu.uta.ventas.session.SesionActiva;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacionServiceTest {

    private UsuarioRepositoryFalso repository;
        private SesionActiva sesionActiva;
        private PasswordEncoder passwordEncoder;
        private AutenticacionService service;

    @BeforeEach
    void setUp() {

        repository = new UsuarioRepositoryFalso();

        sesionActiva =new SesionActiva();

        passwordEncoder =new Sha256PasswordEncoder();

        service = new AutenticacionService(repository,sesionActiva,passwordEncoder);
}

    @Test
    void debeIniciarSesionConCredencialesCorrectas() {

        repository.agregarUsuario(
        crearUsuario(1,"Carlos","Perez","cperez","CAJERO",true),
        passwordEncoder.encode("123456"));

        Usuario resultado = service.iniciarSesion("cperez","123456");

        assertNotNull(resultado);

        assertEquals(
                "cperez",
                resultado.getUsername()
        );

        assertTrue(
                sesionActiva.haySesionActiva()
        );

        assertEquals(
                resultado,
                sesionActiva.getUsuarioActual()
        );
    }

    @Test
    void debeIniciarSesionComoAdministrador() {

        repository.agregarUsuario(
                crearUsuario(
                        1,
                        "Administrador",
                        "Principal",
                        "admin",
                        "ADMIN",
                        true
                ),
                passwordEncoder.encode("123456")
        );

        Usuario resultado =
                service.iniciarSesion(
                        "admin",
                        "123456"
                );

        assertNotNull(resultado);

        assertTrue(
                sesionActiva.esAdmin()
        );

        assertFalse(
                sesionActiva.esCajero()
        );
    }

    @Test
    void debeIniciarSesionComoCajero() {

        repository.agregarUsuario(
                crearUsuario(
                        2,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        true
                ),
                 passwordEncoder.encode("123456")
        );

        service.iniciarSesion(
                "cperez",
                "123456"
        );

        assertTrue(
                sesionActiva.esCajero()
        );

        assertFalse(
                sesionActiva.esAdmin()
        );
    }

    @Test
    void noDebeAceptarUsernameIncorrecto() {

        repository.agregarUsuario(
                crearUsuario(
                        1,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        true
                ),
                 passwordEncoder.encode("123456")
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.iniciarSesion(
                                "usuarioIncorrecto",
                                "123456"
                        )
                );

        assertEquals(
                "Usuario o contraseña incorrectos.",
                exception.getMessage()
        );

        assertFalse(
                sesionActiva.haySesionActiva()
        );
    }

    @Test
    void noDebeAceptarPasswordIncorrecto() {

        repository.agregarUsuario(
                crearUsuario(
                        1,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        true
                ),
                 passwordEncoder.encode("123456")
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.iniciarSesion(
                                "cperez",
                                "incorrecta"
                        )
                );

        assertEquals(
                "Usuario o contraseña incorrectos.",
                exception.getMessage()
        );

        assertFalse(
                sesionActiva.haySesionActiva()
        );
    }

    @Test
    void noDebeAceptarUsuarioVacio() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.iniciarSesion(
                                "",
                                "123456"
                        )
                );

        assertEquals(
                "Ingrese el nombre de usuario.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeAceptarPasswordVacio() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.iniciarSesion(
                                "cperez",
                                ""
                        )
                );

        assertEquals(
                "Ingrese la contraseña.",
                exception.getMessage()
        );
    }

    @Test
    void noDebePermitirUsuarioInactivo() {

        repository.agregarUsuario(
                crearUsuario(
                        1,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        false
                ),
                 passwordEncoder.encode("123456")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.iniciarSesion(
                        "cperez",
                        "123456"
                )
        );

        assertFalse(
                sesionActiva.haySesionActiva()
        );
    }

    @Test
    void debeEliminarSesionAlCerrarSesion() {

        repository.agregarUsuario(
                crearUsuario(
                        1,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        true
                ),
                 passwordEncoder.encode("123456")
        );

        service.iniciarSesion(
                "cperez",
                "123456"
        );

        assertTrue(
                sesionActiva.haySesionActiva()
        );

        service.cerrarSesion();

        assertFalse(
                sesionActiva.haySesionActiva()
        );
    }

    private Usuario crearUsuario(
            int id,
            String nombre,
            String apellido,
            String username,
            String rol,
            boolean activo) {

        return new Usuario(
                id,
                nombre,
                apellido,
                username,
                null,
                rol,
                activo
        );
    }

    
    private static class UsuarioRepositoryFalso
            implements UsuarioRepository {

        private final List<Usuario> usuarios =
                new ArrayList<>();

        private final List<String> passwordHashes =
                new ArrayList<>();

        void agregarUsuario(
                Usuario usuario,
                String passwordHash) {

            usuarios.add(usuario);
            passwordHashes.add(passwordHash);
        }

        @Override
        public Optional<Usuario> autenticar(
                String username,
                String passwordHash) {

            for (int i = 0;
                 i < usuarios.size();
                 i++) {

                Usuario usuario =
                        usuarios.get(i);

                String hash =
                        passwordHashes.get(i);

                if (usuario.isActivo()
                        && usuario.getUsername()
                                .equals(username)
                        && hash.equals(
                                passwordHash)) {

                    return Optional.of(usuario);
                }
            }

            return Optional.empty();
        }

        @Override
        public List<Usuario> listarTodos() {
            return new ArrayList<>(usuarios);
        }

        @Override
        public List<Usuario> buscar(
                String texto) {

            return usuarios.stream()
                    .filter(usuario ->
                            usuario.getUsername()
                                    .contains(texto))
                    .toList();
        }

        @Override
        public Optional<Usuario> buscarPorId(
                int id) {

            return usuarios.stream()
                    .filter(usuario ->
                            usuario.getId() == id)
                    .findFirst();
        }

        @Override
        public boolean existeUsername(
                String username,
                int idExcluir) {

            return usuarios.stream()
                    .anyMatch(usuario ->
                            usuario.getId()
                                    != idExcluir
                            && usuario
                                    .getUsername()
                                    .equals(username));
        }

        @Override
        public boolean guardar(
                Usuario usuario,
                String passwordHash) {

            agregarUsuario(
                    usuario,
                    passwordHash
            );

            return true;
        }

        @Override
        public boolean actualizar(
                Usuario usuario) {

            return buscarPorId(
                    usuario.getId()
            ).isPresent();
        }

        @Override
        public boolean desactivar(int id) {

            Optional<Usuario> usuario =
                    buscarPorId(id);

            if (usuario.isEmpty()) {
                return false;
            }

            usuario.get()
                    .setActivo(false);

            return true;
        }

        @Override
        public boolean cambiarPassword(
                int id,
                String passwordHash) {

            for (int i = 0;
                 i < usuarios.size();
                 i++) {

                if (usuarios.get(i)
                        .getId() == id) {

                    passwordHashes.set(
                            i,
                            passwordHash
                    );

                    return true;
                }
            }

            return false;
        }
    }
}