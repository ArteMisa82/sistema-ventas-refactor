package ec.edu.uta.ventas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    private int id;
    private String numeroFactura;
    private LocalDateTime fecha;

    private String cliente;
    private Cliente clienteObj;

    private Usuario usuario;

    private List<DetalleVenta> detalles;

    private BigDecimal subtotal;
    private BigDecimal porcentajeIva;
    private BigDecimal iva;
    private BigDecimal total;

    private boolean anulada;

    public Venta() {

        this.fecha =
                LocalDateTime.now();

        this.cliente =
                "Consumidor Final";

        this.detalles =
                new ArrayList<>();

        this.subtotal =
                BigDecimal.ZERO;

        this.porcentajeIva =
                BigDecimal.ZERO;

        this.iva =
                BigDecimal.ZERO;

        this.total =
                BigDecimal.ZERO;

        this.anulada = false;
    }

    public void calcularTotales(
            BigDecimal porcentajeIva) {

        if (porcentajeIva == null) {
            throw new IllegalArgumentException(
                    "El porcentaje de IVA es obligatorio."
            );
        }

        if (porcentajeIva.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El porcentaje de IVA no puede ser negativo."
            );
        }

        this.porcentajeIva =
                porcentajeIva;

        this.subtotal =
                detalles.stream()
                        .map(
                                DetalleVenta::
                                        getSubtotalItem
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        this.iva =
                subtotal
                        .multiply(
                                porcentajeIva
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        this.total =
                subtotal.add(iva);
    }

    public void agregarDetalle(
            DetalleVenta detalle) {

        if (detalle == null) {
            throw new IllegalArgumentException(
                    "El detalle es obligatorio."
            );
        }

        detalles.add(detalle);

        calcularTotales(
                porcentajeIva
        );
    }

    public void eliminarDetalle(
            int indice) {

        if (indice < 0 ||
                indice >= detalles.size()) {

            throw new IllegalArgumentException(
                    "El detalle seleccionado no existe."
            );
        }

        detalles.remove(indice);

        calcularTotales(
                porcentajeIva
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(
            String numeroFactura) {

        this.numeroFactura =
                numeroFactura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(
            LocalDateTime fecha) {

        this.fecha = fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(
            String cliente) {

        this.cliente = cliente;
    }

    public Cliente getClienteObj() {
        return clienteObj;
    }

    public void setClienteObj(
            Cliente clienteObj) {

        this.clienteObj =
                clienteObj;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(
            Usuario usuario) {

        this.usuario = usuario;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(
            List<DetalleVenta> detalles) {

        this.detalles =
                detalles != null
                        ? detalles
                        : new ArrayList<>();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal) {

        this.subtotal = subtotal;
    }

    public BigDecimal getPorcentajeIva() {
        return porcentajeIva;
    }

    public void setPorcentajeIva(
            BigDecimal porcentajeIva) {

        this.porcentajeIva =
                porcentajeIva;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(
            BigDecimal iva) {

        this.iva = iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(
            BigDecimal total) {

        this.total = total;
    }

    public boolean isAnulada() {
        return anulada;
    }

    public void setAnulada(
            boolean anulada) {

        this.anulada = anulada;
    }
}