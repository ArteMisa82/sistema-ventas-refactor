package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.repository.UsuarioRepository;
import ec.edu.uta.ventas.security.PasswordEncoder;

import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_CAJERO = "CAJERO";
    private static final String ADMIN_PRINCIPAL = "admin";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.listarTodos();
    }

    public List<Usuario> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return listarTodos();
        }

        return usuarioRepository.buscar(
                texto.trim()
        );
    }

    public Optional<Usuario> buscarPorId(int id) {

        validarId(id);
        return usuarioRepository.buscarPorId(id);
    }

    public boolean registrar(String nombre,String apellido,String username,String password,String rol) {

        validarDatosUsuario(nombre,apellido,username,rol);
        validarPassword(password);

        String usernameNormalizado =username.trim();

        if (usuarioRepository.existeUsername(usernameNormalizado,0)) {

            throw new IllegalArgumentException("El nombre de usuario ya existe."
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre.trim());
        usuario.setApellido(apellido.trim());
        usuario.setUsername(usernameNormalizado);
        usuario.setRol(rol.trim().toUpperCase());
        usuario.setActivo(true);

        String passwordHash =passwordEncoder.encode(password);

        if (!usuarioRepository.guardar(usuario,passwordHash)) {

            throw new IllegalStateException("No se pudo registrar el usuario."
            );
        }

        return true;
    }

    public boolean actualizar(int id,String nombre,String apellido,String username,String rol,boolean activo) {

        validarId(id);

        validarDatosUsuario(nombre,apellido,username,rol);

        Usuario usuarioExistente = usuarioRepository.buscarPorId(id).orElseThrow(() ->
                new IllegalArgumentException("El usuario no existe."));

        /*
         * El username no se puede modificar durante la edición.
         */
        if (!usuarioExistente.getUsername().equals(username.trim())) {

            throw new IllegalArgumentException("El nombre de usuario no puede modificarse.");
        }

        if (usuarioRepository.existeUsername(username.trim(),id)) {

            throw new IllegalArgumentException("El nombre de usuario ya existe."
            );
        }

        /*
         * Protección del administrador principal.
         * No puede quedar inactivo mediante edición.
         */
        if (esAdminPrincipal(usuarioExistente) && !activo) {

            throw new IllegalArgumentException("No puede desactivar el administrador principal."
            );
        }

        usuarioExistente.setNombre(nombre.trim());
        usuarioExistente.setApellido(apellido.trim());
        usuarioExistente.setRol(rol.trim().toUpperCase());
        usuarioExistente.setActivo(activo);

        if (!usuarioRepository.actualizar(usuarioExistente)) {

            throw new IllegalStateException("No se pudo actualizar el usuario.");
        }

        return true;
    }

    public boolean desactivar(int id) {

        validarId(id);

        Usuario usuario = usuarioRepository.buscarPorId(id).orElseThrow(
                () -> new IllegalArgumentException("El usuario no existe."));

        if (esAdminPrincipal(usuario)) {

            throw new IllegalArgumentException("No puede desactivar el administrador principal.");
        }

        if (!usuario.isActivo()) {

            throw new IllegalArgumentException("El usuario ya se encuentra inactivo.");
        }

        if (!usuarioRepository.desactivar(id)) {

            throw new IllegalStateException("No se pudo desactivar el usuario.");
        }

        return true;
    }

    public boolean cambiarPassword(int id,String nuevaPassword) {

        validarId(id);
        validarPassword(nuevaPassword);

        if (usuarioRepository.buscarPorId(id).isEmpty()) {

            throw new IllegalArgumentException( "El usuario no existe.");
        }

        String passwordHash = passwordEncoder.encode(nuevaPassword);

        if (!usuarioRepository.cambiarPassword(id,passwordHash)) {

            throw new IllegalStateException("No se pudo actualizar la contraseña."
            );
        }

        return true;
    }

    private void validarDatosUsuario(String nombre, String apellido, String username, String rol) {

        if (nombre == null|| nombre.isBlank()) {

            throw new IllegalArgumentException(  "El nombre es obligatorio.");
        }

        if (apellido == null || apellido.isBlank()) {

            throw new IllegalArgumentException("El apellido es obligatorio.");
        }

        if (username == null || username.isBlank()) {

            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        validarRol(rol);
    }

    private void validarRol(String rol) {

        if (rol == null || rol.isBlank()) {

            throw new IllegalArgumentException(  "El rol es obligatorio.");
        }

        String rolNormalizado = rol.trim().toUpperCase();

        if (!ROL_ADMIN.equals(rolNormalizado) && !ROL_CAJERO.equals(rolNormalizado)) {

            throw new IllegalArgumentException("El rol debe ser ADMIN o CAJERO."
            );
        }
    }

    private void validarPassword(String password) {

        if (password == null|| password.isBlank()) {

            throw new IllegalArgumentException("Ingrese una contraseña.");
        }

        if (password.length() < 6) {

            throw new IllegalArgumentException("La contraseña debe tener mínimo 6 caracteres.");
        }
    }

    private void validarId(int id) {

        if (id <= 0) {

            throw new IllegalArgumentException("El identificador del usuario no es válido.");
        }
    }

    private boolean esAdminPrincipal(Usuario usuario) {

        return usuario.getUsername() != null && usuario.getUsername().equalsIgnoreCase(ADMIN_PRINCIPAL);
    }
}