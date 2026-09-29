package ec.edu.uta.ventas.repository;

import ec.edu.uta.ventas.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    
    List<Cliente> listarActivos();

    List<Cliente> listarInactivos();

    List<Cliente> buscar(String texto);

    Optional<Cliente> buscarPorCedula(String cedula);

    Optional<Cliente> buscarInactivoPorCedula(String cedula);

    boolean existeCedula(String cedula, int idExcluir);

    boolean guardar(Cliente cliente);

    boolean actualizar(Cliente cliente);

    boolean desactivar(int id);

    boolean reactivar(int id);
}
