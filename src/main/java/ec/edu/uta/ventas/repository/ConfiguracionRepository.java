package ec.edu.uta.ventas.repository;

import ec.edu.uta.ventas.model.Configuracion;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionRepository {

    List<Configuracion> listar();

    Optional<Configuracion> buscarPorClave(
            String clave);

    boolean actualizar(
            String clave,
            String valor);
}