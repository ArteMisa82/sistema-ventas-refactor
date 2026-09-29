package ec.edu.uta.ventas.repository;

import ec.edu.uta.ventas.model.DetalleVenta;
import ec.edu.uta.ventas.model.Venta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VentaRepository {

    Venta guardar(Venta venta);

    Optional<Venta> buscarPorId(int id);

    Optional<Venta> buscarPorNumeroFactura(
            String numeroFactura
    );

    List<Venta> listar();

    List<Venta> buscarPorFecha(
            LocalDate fecha
    );

    List<DetalleVenta> listarDetalles(
            int idVenta
    );

    boolean anular(
            int idVenta
    );
}