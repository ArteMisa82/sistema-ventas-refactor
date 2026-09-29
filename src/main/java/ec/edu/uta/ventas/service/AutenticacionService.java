package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.repository.UsuarioRepository;
import ec.edu.uta.ventas.security.PasswordEncoder;
import ec.edu.uta.ventas.session.SesionActiva;

import java.util.Optional;

public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final SesionActiva sesionActiva;
    private final PasswordEncoder passwordEncoder;

    public AutenticacionService(UsuarioRepository usuarioRepository,SesionActiva sesionActiva,PasswordEncoder passwordEncoder) {

        this.usuarioRepository =usuarioRepository;
        this.sesionActiva =sesionActiva;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario iniciarSesion(String username,String password) {

        validarCredenciales(username,password);

        String usernameNormalizado = username.trim();
        String passwordHash = passwordEncoder.encode(password);

        Optional<Usuario> usuario = usuarioRepository.autenticar(usernameNormalizado,passwordHash);

        if (usuario.isEmpty()) {

            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }

        Usuario usuarioAutenticado =usuario.get();
        sesionActiva.iniciarSesion(usuarioAutenticado);

        return usuarioAutenticado;
    }

    private void validarCredenciales(String username,String password) {

        if (username == null || username.isBlank()) {

            throw new IllegalArgumentException( "Ingrese el nombre de usuario.");
        }

        if (password == null || password.isBlank()) {

            throw new IllegalArgumentException("Ingrese la contraseña.");
        }
    }

    public void cerrarSesion() {
        sesionActiva.cerrarSesion();
    }
}