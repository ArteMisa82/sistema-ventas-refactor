package ec.edu.uta.ventas.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Responsable de cargar la configuración necesaria
 * para acceder a la base de datos.
 */
public class DatabaseConfig {
    
    private final Properties properties = new Properties();

    public DatabaseConfig() {
        loadProperties();
    }

    private void loadProperties() {

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "No se encontró el archivo application.properties");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo cargar la configuración de la base de datos",
                    e);
        }
    }

    public String getUrl() {
        return obtenerConfiguracion("DB_URL", "db.url");
    }

    public String getUsername() {
        return obtenerConfiguracion("DB_USERNAME", "db.username");
    }

    public String getPassword() {
        return obtenerConfiguracion("DB_PASSWORD", "db.password");
    }

    private String obtenerConfiguracion(String variableEntorno, String propiedad) {
        String valor = System.getenv(variableEntorno);
        return valor != null ? valor : properties.getProperty(propiedad);
    }
}
