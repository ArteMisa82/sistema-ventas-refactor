package ec.edu.uta.ventas.session;

import ec.edu.uta.ventas.model.Usuario;

public class SesionActiva {

    private Usuario usuarioActual;

    public boolean iniciarSesion(
            Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario de la sesión es obligatorio."
            );
        }

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException(
                    "El usuario se encuentra inactivo."
            );
        }

        usuarioActual = usuario;

        return true;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public Usuario getUsuarioActual() {

        if (!haySesionActiva()) {
            throw new IllegalStateException(
                    "No existe una sesión activa."
            );
        }

        return usuarioActual;
    }

    public boolean esAdmin() {

        return haySesionActiva()
                && usuarioActual.isAdmin();
    }

    public boolean esCajero() {

        return haySesionActiva()
                && usuarioActual.isCajero();
    }
}