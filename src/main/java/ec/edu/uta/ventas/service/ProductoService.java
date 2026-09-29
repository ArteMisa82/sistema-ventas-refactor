package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Producto;
import ec.edu.uta.ventas.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarActivos() {
        return productoRepository.listarActivos();
    }

    public List<Producto> listarInactivos() {
        return productoRepository.listarInactivos();
    }


    public Optional<Producto> buscarPorId(int id) {

        validarId(id);

        return productoRepository.buscarPorId(id);
    }

    public String obtenerSiguienteCodigo() {
        return productoRepository.obtenerSiguienteCodigo();
    }

    public boolean registrar(
            String nombre,
            BigDecimal precio,
            int stock,
            String codigoBarras) {

        validarDatos(nombre, precio, stock);
        validarCodigoBarrasDuplicado(codigoBarras, null);

        String codigo = productoRepository.obtenerSiguienteCodigo();

        Producto producto = new Producto(
                codigo,
                nombre.trim(),
                precio,
                stock,
                normalizarCodigoBarras(codigoBarras)
        );

        if (!productoRepository.guardar(producto)) {
            throw new IllegalStateException("No se pudo registrar el producto.");
        }

        return true;
    }

    public boolean actualizar(
            int id,
            String nombre,
            BigDecimal precio,
            int stock,
            String codigoBarras) {

        validarId(id);
        validarDatos(nombre, precio, stock);

        Producto existente = productoRepository.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe."
                        )
                );

        validarCodigoBarrasDuplicado(codigoBarras, id);

        existente.setNombre(nombre.trim());
        existente.setPrecio(precio);
        existente.setStock(stock);
        existente.setCodigoBarras(
                normalizarCodigoBarras(codigoBarras)
        );

        if (!productoRepository.actualizar(existente)) {
            throw new IllegalStateException("No se pudo actualizar el producto.");
        }

        return true;
    }

    public boolean desactivar(int id) {

        validarId(id);

        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe."
                        )
                );

        if (!producto.isActivo()) {
            throw new IllegalArgumentException(
                    "El producto ya se encuentra inactivo."
            );
        }

        if (!productoRepository.desactivar(id)) {
            throw new IllegalStateException("No se pudo desactivar el producto.");
        }

        return true;
    }

    public boolean reactivar(int id) {

        validarId(id);

        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe."
                        )
                );

        if (producto.isActivo()) {
            throw new IllegalArgumentException(
                    "El producto ya se encuentra activo."
            );
        }

        if (!productoRepository.reactivar(id)) {
            throw new IllegalStateException( "No se pudo reactivar el producto.");
        }

        return true;
    }

    private void validarDatos(
            String nombre,
            BigDecimal precio,
            int stock) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (precio == null) {
            throw new IllegalArgumentException(
                    "El precio es obligatorio."
            );
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero."
            );
        }

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo."
            );
        }
    }

    private void validarCodigoBarrasDuplicado(
            String codigoBarras,
            Integer idActual) {

        String codigoNormalizado =
                normalizarCodigoBarras(codigoBarras);

        if (codigoNormalizado == null) {
            return;
        }

        Optional<Producto> existente =
                productoRepository.buscarPorCodigoBarras(
                        codigoNormalizado
                );

        if (existente.isEmpty()) {
            return;
        }

        if (idActual == null ||
                existente.get().getId() != idActual) {

            throw new IllegalArgumentException(
                    "El código de barras ya está registrado."
            );
        }
    }

    private String normalizarCodigoBarras(
            String codigoBarras) {

        if (codigoBarras == null ||
                codigoBarras.isBlank()) {

            return null;
        }

        return codigoBarras.trim();
    }

    private void validarId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del producto no es válido."
            );
        }
    }

    public List<Producto> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return productoRepository.listarActivos();
        }

        return productoRepository.buscar(texto.trim());
    }

    public Optional<Producto> buscarPorCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }

        return productoRepository.buscarPorCodigo(
                codigo.trim()
        );
    }

    public Optional<Producto> buscarPorCodigoBarras(
            String codigoBarras) {

        if (codigoBarras == null ||
                codigoBarras.isBlank()) {

            return Optional.empty();
        }

        return productoRepository
                .buscarPorCodigoBarras(
                        codigoBarras.trim()
                );
    }
}