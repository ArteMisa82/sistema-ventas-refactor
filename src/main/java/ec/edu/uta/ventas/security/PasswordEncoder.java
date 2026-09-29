package ec.edu.uta.ventas.security;

public interface PasswordEncoder {
    
    String encode(String password);

    /**
     * Verifica si una contraseña en texto plano corresponde
     * con una contraseña previamente codificada.
     *
     * @param rawPassword contraseña en texto plano
     * @param encodedPassword contraseña codificada
     * @return true si coinciden
     */
    boolean matches(String rawPassword, String encodedPassword);
}
