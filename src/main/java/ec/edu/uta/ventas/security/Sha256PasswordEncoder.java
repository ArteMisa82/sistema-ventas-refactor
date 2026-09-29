package ec.edu.uta.ventas.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementación de PasswordEncoder utilizando SHA-256.
 *
 * Mantiene compatibilidad con las contraseñas existentes del sistema,
 * pero desacopla el algoritmo de los servicios.
 */
public class Sha256PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(String password) {

        if (password == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexadecimal = new StringBuilder();

            for (byte b : hash) {
                hexadecimal.append(String.format("%02x", b));
            }

            return hexadecimal.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo inicializar SHA-256",e);
        }
    }

    @Override
    public boolean matches(String rawPassword,String encodedPassword) {

        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        return encode(rawPassword).equalsIgnoreCase(encodedPassword);
    }
}