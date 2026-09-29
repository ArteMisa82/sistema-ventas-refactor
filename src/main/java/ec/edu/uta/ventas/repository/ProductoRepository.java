package ec.edu.uta.ventas.repository;

import ec.edu.uta.ventas.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    
    List<Producto> listarActivos();

    List<Producto> listarInactivos();

    List<Producto> buscar(String texto);

    Optional<Producto> buscarPorId(int id);

    Optional<Producto> buscarPorCodigo(String codigo);

    Optional<Producto> buscarPorCodigoBarras(String codigoBarras);

    String obtenerSiguienteCodigo();

    boolean guardar(Producto producto);

    boolean actualizar(Producto producto);

    boolean desactivar(int id);

    boolean reactivar(int id);
}
