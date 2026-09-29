package ec.edu.uta.ventas.repository;

import ec.edu.uta.ventas.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    List<Usuario> listarTodos();

    List<Usuario> buscar(String texto);

    Optional<Usuario> buscarPorId(int id);

    boolean existeUsername(String username, int idExcluir);

    boolean guardar(Usuario usuario, String passwordHash);

    boolean actualizar(Usuario usuario);

    boolean desactivar(int id);

    boolean cambiarPassword(int id, String passwordHash);

    Optional<Usuario> autenticar(String username,String passwordHash);
}