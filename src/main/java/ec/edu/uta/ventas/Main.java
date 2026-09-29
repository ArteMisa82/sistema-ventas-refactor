package ec.edu.uta.ventas;

import ec.edu.uta.ventas.config.DatabaseConfig;
import ec.edu.uta.ventas.database.ConnectionProvider;
import ec.edu.uta.ventas.database.PostgresConnectionProvider;
import ec.edu.uta.ventas.repository.*;
import ec.edu.uta.ventas.repository.jdbc.*;
import ec.edu.uta.ventas.security.PasswordEncoder;
import ec.edu.uta.ventas.security.Sha256PasswordEncoder;
import ec.edu.uta.ventas.service.*;
import ec.edu.uta.ventas.session.SesionActiva;
import ec.edu.uta.ventas.view.LoginView;
import ec.edu.uta.ventas.view.MenuPrincipalView;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DatabaseConfig databaseConfig = new DatabaseConfig();
            ConnectionProvider connectionProvider = new PostgresConnectionProvider(databaseConfig);
            PasswordEncoder passwordEncoder = new Sha256PasswordEncoder();
            SesionActiva sesionActiva = new SesionActiva();

            ClienteRepository clienteRepository = new ClienteJdbcRepository(connectionProvider);
            ClienteService clienteService = new ClienteService(clienteRepository);

            ProductoRepository productoRepository = new ProductoJdbcRepository(connectionProvider);
            ProductoService productoService = new ProductoService(productoRepository);

            UsuarioRepository usuarioRepository = new UsuarioJdbcRepository(connectionProvider);
            UsuarioService usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);

            ConfiguracionRepository configuracionRepository =
                    new ConfiguracionJdbcRepository(connectionProvider);
            ConfiguracionService configuracionService =
                    new ConfiguracionService(configuracionRepository);

            AutenticacionService autenticacionService =
                    new AutenticacionService(usuarioRepository, sesionActiva, passwordEncoder);

            VentaRepository ventaRepository = new VentaJdbcRepository(connectionProvider);
            VentaService ventaService =
                    new VentaService(ventaRepository, configuracionService, sesionActiva);

            ReporteFacturaService reporteFacturaService =
                    new ReporteFacturaService(connectionProvider, configuracionService);

            Runnable[] mostrarLogin = new Runnable[1];

            Runnable mostrarMenu = () -> {
                MenuPrincipalView menu = new MenuPrincipalView(
                        clienteService,
                        productoService,
                        usuarioService,
                        configuracionService,
                        ventaService,
                        reporteFacturaService,
                        autenticacionService,
                        sesionActiva,
                        () -> mostrarLogin[0].run()
                );

                menu.setVisible(true);
            };

            mostrarLogin[0] = () -> {
                LoginView login = new LoginView(autenticacionService, mostrarMenu);
                login.setVisible(true);
            };

            mostrarLogin[0].run();
        });
    }
}