package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Cliente;
import ec.edu.uta.ventas.model.DetalleVenta;
import ec.edu.uta.ventas.model.Producto;
import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.model.Venta;
import ec.edu.uta.ventas.repository.VentaRepository;
import ec.edu.uta.ventas.session.SesionActiva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class VentaService {

        private final VentaRepository ventaRepository;
        private final ConfiguracionService configuracionService;
        private final SesionActiva sesionActiva;

        public VentaService(VentaRepository ventaRepository,ConfiguracionService configuracionService,SesionActiva sesionActiva) {

                this.ventaRepository =ventaRepository;
                this.configuracionService =configuracionService;
                this.sesionActiva =sesionActiva;
        }

        public Venta crearVenta(Cliente cliente,List<DetalleVenta> detalles) {

                if (!sesionActiva.haySesionActiva()) {

                        throw new IllegalStateException("Debe iniciar sesión para registrar una venta." );
                }

                if (detalles == null || detalles.isEmpty()) {

                        throw new IllegalArgumentException("La venta debe contener al menos un producto.");
                }

                validarDetalles(detalles);

                Usuario usuario = sesionActiva.getUsuarioActual();
                BigDecimal porcentajeIva = configuracionService.obtenerIva();
                Venta venta =new Venta();

                venta.setUsuario(usuario);

                if (cliente != null) {

                        venta.setClienteObj(cliente);
                        venta.setCliente(obtenerNombreCliente(cliente));
                }

                venta.setDetalles(detalles);
                venta.calcularTotales(porcentajeIva);

                return ventaRepository.guardar(venta);
        }

        public Venta calcularTotales(List<DetalleVenta> detalles) {

                Venta venta = new Venta();

                if (detalles == null || detalles.isEmpty()) {
                        venta.setDetalles(List.of());
                        venta.calcularTotales( configuracionService.obtenerIva());
                        return venta;
                }

                validarDetalles(detalles);
                venta.setDetalles(detalles);
                venta.calcularTotales( configuracionService.obtenerIva());

                 return venta;
        }

        private void validarDetalles(List<DetalleVenta> detalles) {

                for (DetalleVenta detalle :detalles) {

                        if (detalle == null) {

                                throw new IllegalArgumentException("La venta contiene un detalle inválido.");
                        }

                        Producto producto = detalle.getProducto();

                        if (producto == null) {
                                throw new IllegalArgumentException( "El producto del detalle es obligatorio.");
                        }

                        if (producto.getId() <= 0) {
                                throw new IllegalArgumentException("El producto del detalle no es válido.");
                        }

                        if (!producto.isActivo()) {
                                throw new IllegalArgumentException("El producto "+ producto.getNombre() + " se encuentra inactivo.");
                        }

                        if (detalle.getCantidad() <= 0) {
                                throw new IllegalArgumentException("La cantidad del producto " + producto.getNombre()+ " debe ser mayor a cero.");
                        }

                        if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                                throw new IllegalArgumentException("El precio del producto " + producto.getNombre() + " no es válido.");
                        }

                        if (detalle.getCantidad() > producto.getStock()) {
                                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock());
                        }
                }
        }


        public List<DetalleVenta> listarDetalles(int idVenta) {

                if (idVenta <= 0) {
                        throw new IllegalArgumentException("El ID de la venta no es válido.");
                }

                return ventaRepository.listarDetalles(
                        idVenta
                );
        }

        public List<Venta> listar() {

                return ventaRepository.listar();
        }

        public Venta buscarPorId(int id) {

                if (id <= 0) {

                        throw new IllegalArgumentException("El identificador de la venta no es válido.");
                }

                return ventaRepository.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("La venta no existe."));
        }

        public Optional<Venta> buscarPorNumeroFactura(String numeroFactura) {

                if (numeroFactura == null || numeroFactura.isBlank()) {

                        throw new IllegalArgumentException("El número de factura es obligatorio.");
                }

                return ventaRepository.buscarPorNumeroFactura(numeroFactura.trim());
        }

        public List<Venta> buscarPorFecha(LocalDate fecha) {

                if (fecha == null) {

                        throw new IllegalArgumentException("La fecha es obligatoria.");
                }

                return ventaRepository.buscarPorFecha(fecha);
        }

        public boolean anular(int idVenta) {

                if (!sesionActiva.haySesionActiva()) {

                        throw new IllegalStateException( "Debe iniciar sesión.");
                }

                if (!sesionActiva.esAdmin()) {

                        throw new IllegalStateException("Solo un administrador puede anular ventas.");
                }

                if (idVenta <= 0) {

                        throw new IllegalArgumentException("El identificador de la venta no es válido.");
                }

                boolean anulada = ventaRepository.anular(idVenta);

                if (!anulada) {

                        throw new IllegalArgumentException("La venta no existe o ya se encuentra anulada.");
                }

                return true;
        }


        private String obtenerNombreCliente(Cliente cliente) {

                StringBuilder nombre = new StringBuilder();

                agregarParteNombre(nombre,cliente.getNombre());
                agregarParteNombre(nombre,cliente.getSegundoNombre());
                agregarParteNombre(nombre, cliente.getApellido());

                agregarParteNombre(nombre,cliente.getSegundoApellido());

                String resultado = nombre.toString().trim();

                if (resultado.isBlank()) {

                        return "Consumidor Final";
                }

                return resultado;
        }

        private void agregarParteNombre(StringBuilder nombre,String parte) {

                if (parte == null || parte.isBlank()) {

                        return;
                }

                if (!nombre.isEmpty()) {
                        nombre.append(" ");
                }

                nombre.append(parte.trim());
        }
}