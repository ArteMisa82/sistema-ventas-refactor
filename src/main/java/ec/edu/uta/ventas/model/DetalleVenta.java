package ec.edu.uta.ventas.model;

import java.math.BigDecimal;

public class DetalleVenta {

    private int id;
    private int idVenta;
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalItem;

    public DetalleVenta() {
    }

    public DetalleVenta(
            Producto producto,
            int cantidad) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio."
            );
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero."
            );
        }

        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario =
                producto.getPrecio();

        recalcular();
    }

    public void recalcular() {

        if (precioUnitario == null) {
            subtotalItem =
                    BigDecimal.ZERO;
            return;
        }

        subtotalItem =
                precioUnitario.multiply(
                        BigDecimal.valueOf(cantidad)
                );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(
            Producto producto) {

        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(
            int cantidad) {

        this.cantidad = cantidad;
        recalcular();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(
            BigDecimal precioUnitario) {

        this.precioUnitario =
                precioUnitario;

        recalcular();
    }

    public BigDecimal getSubtotalItem() {
        return subtotalItem;
    }

    public void setSubtotalItem(
            BigDecimal subtotalItem) {

        this.subtotalItem =
                subtotalItem;
    }
}