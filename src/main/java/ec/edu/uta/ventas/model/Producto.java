package ec.edu.uta.ventas.model;

import java.math.BigDecimal;

public class Producto {

    private int id;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private String codigoBarras;//Puede ser null si no se utiliza
    private boolean activo;// indica si el producto esta disponible(true) o no(false) 

    public Producto() {
        //Se usa cuando primero se crea el objeto y luego se llenan sus atributos con setters.
    }
    
    //Constructor completo
    public Producto(int id, String codigo, String nombre, BigDecimal precio, int stock, String codigoBarras, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.codigoBarras = codigoBarras;
        this.activo = activo;
    }

    //Constructor simplificado para el registro de un nuevo producto
    public Producto(String codigo, String nombre, BigDecimal precio, int stock, String codigoBarras) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.codigoBarras = codigoBarras;
        this.activo = true;
    }
    
    /**
     * Verifica si existe suficiente stock.
     */
    public boolean hayStock(int cantidad){
        return stock >= cantidad;
    }
    
    /**
     * Calcula el subtotal del producto.
     *
     * Fórmula:
     * subtotal = precio × cantidad
     * Se usa multiply() porque BigDecimal no usa operadores
     * aritméticos como *, +, -, /.
     */

    public BigDecimal calcularSubtotal(int cantidad){
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
    
}
