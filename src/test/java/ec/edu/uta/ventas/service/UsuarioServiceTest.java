package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.repository.UsuarioRepository;
import ec.edu.uta.ventas.security.PasswordEncoder;
import ec.edu.uta.ventas.security.Sha256PasswordEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

    private UsuarioRepositoryFalso repository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService service;
    

    @BeforeEach
    void setUp() {

        repository = new UsuarioRepositoryFalso();
        passwordEncoder =new Sha256PasswordEncoder();
        service =new UsuarioService(repository,passwordEncoder);
    }

    @Test
    void debeRegistrarUsuarioValido() {

        boolean resultado = service.registrar(
                "Carlos",
                "Perez",
                "cperez",
                "123456",
                "CAJERO"
        );

        assertTrue(resultado);
        assertEquals(1, repository.usuarios.size());

        Usuario usuario = repository.usuarios.get(0);

        assertEquals("Carlos", usuario.getNombre());
        assertEquals("Perez", usuario.getApellido());
        assertEquals("cperez", usuario.getUsername());
        assertEquals("CAJERO", usuario.getRol());
        assertTrue(usuario.isActivo());

        assertNotNull(repository.ultimoPasswordHash);
        assertNotEquals("123456",repository.ultimoPasswordHash);

        assertEquals(64,repository.ultimoPasswordHash.length());
    }

    @Test
    void noDebeRegistrarNombreVacio() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> service.registrar(
                "","Perez","cperez","123456","CAJERO"));

        assertEquals("El nombre es obligatorio.", exception.getMessage());
    }

    @Test
    void noDebeRegistrarApellidoVacio() {

        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> service.registrar(
                "Carlos","","cperez","123456","CAJERO"));

        assertEquals("El apellido es obligatorio.",exception.getMessage());
    }

    @Test
    void noDebeRegistrarUsernameVacio() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.registrar(
                "Carlos","Perez","","123456","CAJERO"));

        assertEquals("El nombre de usuario es obligatorio.",exception.getMessage());
    }

    @Test
    void noDebeRegistrarPasswordVacio() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> service.registrar(
                "Carlos","Perez","cperez","","CAJERO"));

        assertEquals("Ingrese una contraseña.",exception.getMessage());
    }

    @Test
    void noDebeRegistrarPasswordMenorASeisCaracteres() {

        IllegalArgumentException exception =assertThrows(IllegalArgumentException.class,() -> service.registrar(
                "Carlos","Perez","cperez","123","CAJERO"));

        assertEquals("La contraseña debe tener mínimo 6 caracteres.",exception.getMessage());
    }

    @Test
    void noDebeRegistrarUsernameDuplicado() {

        repository.usuarios.add(crearUsuario(1,"Carlos","Perez","cperez","CAJERO",true));

        IllegalArgumentException exception =assertThrows(IllegalArgumentException.class, () -> service.registrar(
                "Carla","Perez","cperez","123456","CAJERO"));

        assertEquals("El nombre de usuario ya existe.",exception.getMessage());
    }

    @Test
    void debeAceptarRolAdmin() {

        assertDoesNotThrow(() -> service.registrar("Ana","Lopez","alopez","123456", "ADMIN"));

        assertEquals("ADMIN", repository.usuarios.get(0).getRol());
    }

    @Test
    void debeAceptarRolCajero() {

        assertDoesNotThrow(() -> service.registrar("Ana","Lopez","alopez","123456","CAJERO"));

        assertEquals( "CAJERO", repository.usuarios.get(0).getRol());
    }

    @Test
    void noDebeAceptarRolInvalido() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> service.registrar(
                "Ana","Lopez","alopez","123456","SUPERVISOR"));

        assertEquals("El rol debe ser ADMIN o CAJERO.",exception.getMessage());
    }

    @Test
    void debeActualizarUsuario() {

        repository.usuarios.add(crearUsuario(1,"Carlos","Perez","cperez","CAJERO",true));

        boolean resultado = service.actualizar(1,"Carlos Alberto","Perez","cperez","ADMIN",true);

        assertTrue(resultado);

        Usuario usuario = repository.buscarPorId(1).orElseThrow();

        assertEquals("Carlos Alberto",usuario.getNombre());

        assertEquals("ADMIN",usuario.getRol());
    }

    @Test
    void noDebeModificarUsernameAlEditar() {

        repository.usuarios.add(crearUsuario(1,"Carlos","Perez","cperez", "CAJERO",true));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> service.actualizar(
                1,"Carlos","Perez","carlosnuevo","CAJERO",true));

        assertEquals(
                "El nombre de usuario no puede modificarse.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeActualizarUsuarioInexistente() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.actualizar(
                                999,
                                "Carlos",
                                "Perez",
                                "cperez",
                                "CAJERO",
                                true
                        )
                );

        assertEquals(
                "El usuario no existe.",
                exception.getMessage()
        );
    }

    @Test
    void debeDesactivarUsuario() {

        repository.usuarios.add(
                crearUsuario(
                        1,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        true
                )
        );

        boolean resultado =
                service.desactivar(1);

        assertTrue(resultado);

        Usuario usuario =
                repository.buscarPorId(1)
                        .orElseThrow();

        assertFalse(usuario.isActivo());
    }

    @Test
    void noDebeDesactivarUsuarioYaInactivo() {

        repository.usuarios.add(
                crearUsuario(
                        1,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        false
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.desactivar(1)
                );

        assertEquals(
                "El usuario ya se encuentra inactivo.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeDesactivarAdminPrincipal() {

        repository.usuarios.add(
                crearUsuario(
                        1,
                        "Administrador",
                        "Principal",
                        "admin",
                        "ADMIN",
                        true
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.desactivar(1)
                );

        assertEquals(
                "No puede desactivar el administrador principal.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeDesactivarAdminPrincipalMedianteActualizacion() {

        repository.usuarios.add(
                crearUsuario(
                        1,
                        "Administrador",
                        "Principal",
                        "admin",
                        "ADMIN",
                        true
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizar(
                        1,
                        "Administrador",
                        "Principal",
                        "admin",
                        "ADMIN",
                        false
                )
        );
    }

    @Test
    void debeCambiarPassword() {

        repository.usuarios.add(
                crearUsuario(
                        1,
                        "Carlos",
                        "Perez",
                        "cperez",
                        "CAJERO",
                        true
                )
        );

        boolean resultado =
                service.cambiarPassword(
                        1,
                        "nueva123"
                );

        assertTrue(resultado);

        assertNotNull(
                repository.ultimoPasswordHash
        );

        assertNotEquals(
                "nueva123",
                repository.ultimoPasswordHash
        );

        assertEquals(
                64,
                repository.ultimoPasswordHash.length()
        );
    }

    @Test
    void noDebeCambiarPasswordDeUsuarioInexistente() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.cambiarPassword(
                                999,
                                "nueva123"
                        )
                );

        assertEquals(
                "El usuario no existe.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeAceptarIdInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.desactivar(0)
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

    /*
     * Repositorio falso utilizado para
     * probar UsuarioService sin PostgreSQL.
     */
    private static class UsuarioRepositoryFalso implements UsuarioRepository {

        private final List<Usuario> usuarios =
                new ArrayList<>();

        private String ultimoPasswordHash;

        @Override
        public List<Usuario> listarTodos() {
            return new ArrayList<>(usuarios);
        }

        @Override
        public List<Usuario> buscar(String texto) {

            String filtro =
                    texto.toLowerCase();

            return usuarios.stream()
                    .filter(usuario ->
                            contiene(
                                    usuario.getNombre(),
                                    filtro
                            )
                            || contiene(
                                    usuario.getApellido(),
                                    filtro
                            )
                            || contiene(
                                    usuario.getUsername(),
                                    filtro
                            ))
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
                            usuario.getId() != idExcluir
                            && usuario.getUsername()
                                    .equals(username));
        }

        @Override
        public boolean guardar(
                Usuario usuario,
                String passwordHash) {

            usuario.setId(
                    usuarios.size() + 1
            );

            usuario.setActivo(true);

            ultimoPasswordHash =
                    passwordHash;

            usuarios.add(usuario);

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

            usuario.get().setActivo(false);

            return true;
        }

        @Override
        public boolean cambiarPassword(
                int id,
                String passwordHash) {

            if (buscarPorId(id).isEmpty()) {
                return false;
            }

            ultimoPasswordHash =
                    passwordHash;

            return true;
        }

        private boolean contiene(
                String valor,
                String filtro) {

            return valor != null
                    && valor.toLowerCase()
                            .contains(filtro);
        }

        @Override
        public Optional<Usuario> autenticar(
                String username,
                String passwordHash) {

        return usuarios.stream()
                .filter(usuario ->
                        usuario.getUsername()
                                .equals(username)
                        && usuario.isActivo())
                .findFirst();
        }
    }
}