package ec.edu.uta.ventas.model;

public class Usuario {

    private int id;
    private String nombre;
    private String apellido;
    private String username;
    private String password;
    private String rol;
    private boolean activo;

    public Usuario() {
    }

    public Usuario(
            int id,
            String nombre,
            String apellido,
            String username,
            String password,
            String rol,
            boolean activo) {

        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    public Usuario(
            String nombre,
            String apellido,
            String username,
            String rol) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.rol = rol;
        this.activo = true;
    }

    public boolean isAdmin() {
        return rol != null
                && rol.trim().equalsIgnoreCase("ADMIN");
    }

    public boolean isCajero() {
        return rol != null
                && rol.trim().equalsIgnoreCase("CAJERO");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " [" + rol + "]";
    }
}