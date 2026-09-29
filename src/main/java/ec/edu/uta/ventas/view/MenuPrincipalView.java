package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.service.AutenticacionService;
import ec.edu.uta.ventas.service.ClienteService;
import ec.edu.uta.ventas.service.ConfiguracionService;
import ec.edu.uta.ventas.service.ProductoService;
import ec.edu.uta.ventas.service.ReporteFacturaService;
import ec.edu.uta.ventas.service.UsuarioService;
import ec.edu.uta.ventas.service.VentaService;
import ec.edu.uta.ventas.session.SesionActiva;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipalView extends JFrame {
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final AutenticacionService autenticacionService;
    private final SesionActiva sesionActiva;
    private final Runnable alCerrarSesion;
    private final ConfiguracionService configuracionService;
    private final VentaService ventaService;
    private final ReporteFacturaService reporteFacturaService;

    private JDesktopPane desktopPane;
    private JLabel lblBienvenida;
    private JLabel lblRol;

    private JMenuBar menuBar;

    private JMenu menuProductos;
    private JMenu menuClientes;
    private JMenu menuUsuarios;
    private JMenu menuVentas;
    private JMenu menuConfiguracion;
    private JMenu menuSistema;

    public MenuPrincipalView(
            ClienteService clienteService,
            ProductoService productoService,
            UsuarioService usuarioService,
            ConfiguracionService configuracionService,
            VentaService ventaService,
            ReporteFacturaService reporteFacturaService,
            AutenticacionService autenticacionService,
            SesionActiva sesionActiva,
            Runnable alCerrarSesion) {

        this.clienteService = clienteService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.configuracionService = configuracionService;
        this.ventaService = ventaService;
        this.reporteFacturaService = reporteFacturaService;
        this.autenticacionService = autenticacionService;
        this.sesionActiva = sesionActiva;
        this.alCerrarSesion = alCerrarSesion;

        verificarSesion();
        configurarVentana();
        crearComponentes();
        mostrarUsuario();
        aplicarPermisos();
    }

    private void verificarSesion() {
        if (!sesionActiva.haySesionActiva()) {
            throw new IllegalStateException("No existe una sesión activa.");
        }
    }

    private void configurarVentana() {
        setTitle("Sistema de Ventas");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmarSalida();
            }
        });
    }

    private void crearComponentes() {

        desktopPane = new JDesktopPane();
        desktopPane.setBackground(EstiloUI.FONDO);

        setContentPane(desktopPane);

        lblBienvenida = new JLabel();
        lblRol = new JLabel();

        lblBienvenida.setFont(new Font("SansSerif",Font.BOLD,16));
        lblBienvenida.setForeground(EstiloUI.AZUL_MARINO);

        lblRol.setFont(new Font("SansSerif",Font.PLAIN,13));

        lblRol.setForeground(
                EstiloUI.AZUL_SECUNDARIO
        );

        JPanel panelUsuario =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                20,
                                12
                        )
                );

        panelUsuario.setBackground(
                EstiloUI.BLANCO
        );

        panelUsuario.setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        0,
                        1,
                        0,
                        EstiloUI.BORDE
                )
        );

        panelUsuario.add(
                lblBienvenida
        );

        panelUsuario.add(
                lblRol
        );

        panelUsuario.setBounds(
            0,
            0,
            getWidth(),
            48
    );

        desktopPane.add(
                panelUsuario,
                JLayeredPane.PALETTE_LAYER
        );

        desktopPane.addComponentListener(
            new java.awt.event.ComponentAdapter() {

                @Override
                public void componentResized(
                        java.awt.event.ComponentEvent e) {

                    panelUsuario.setBounds(
                            0,
                            0,
                            desktopPane.getWidth(),
                            48
                    );
                }
            }
    );

        /*
        * =========================
        * MENÚ
        * =========================
        */

        menuBar = new JMenuBar();

        crearMenuProductos(menuBar);
        crearMenuClientes(menuBar);
        crearMenuUsuarios(menuBar);
        crearMenuVentas(menuBar);
        crearMenuConfiguracion(menuBar);
        crearMenuSistema(menuBar);

        configurarEstiloMenu();

        setJMenuBar(menuBar);
    }

    private void crearMenuProductos(JMenuBar menuBar) {
        menuProductos = new JMenu("Productos");
        JMenuItem itemGestionProductos = new JMenuItem("Gestión de Productos");
        itemGestionProductos.addActionListener(e -> abrirProductos());

        menuProductos.add(itemGestionProductos);
        menuBar.add(menuProductos);
    }

    private void crearMenuClientes(JMenuBar menuBar) {
        menuClientes = new JMenu("Clientes");
        JMenuItem itemGestionClientes = new JMenuItem("Gestión de Clientes");
        itemGestionClientes.addActionListener(e -> abrirClientes());

        menuClientes.add(itemGestionClientes);
        menuBar.add(menuClientes);
    }

    private void crearMenuUsuarios(JMenuBar menuBar) {
        menuUsuarios = new JMenu("Usuarios");
        JMenuItem itemGestionUsuarios = new JMenuItem("Gestión de Usuarios");
        itemGestionUsuarios.addActionListener(e -> abrirUsuarios());

        menuUsuarios.add(itemGestionUsuarios);
        menuBar.add(menuUsuarios);
    }

    private void crearMenuVentas(JMenuBar menuBar) {
        menuVentas = new JMenu("Ventas");

        JMenuItem itemNuevaVenta = new JMenuItem("Nueva Venta");
        JMenuItem itemHistorial = new JMenuItem("Historial de Ventas");

        itemNuevaVenta.addActionListener(e -> abrirNuevaVenta());
        itemHistorial.addActionListener(e -> abrirHistorialVentas());

        menuVentas.add(itemNuevaVenta);
        menuVentas.add(itemHistorial);
        menuBar.add(menuVentas);
    }

    private void crearMenuConfiguracion(JMenuBar menuBar) {
        menuConfiguracion = new JMenu("Configuración");
        JMenuItem itemConfiguracion = new JMenuItem("Configuración del Sistema");
        itemConfiguracion.addActionListener(e -> abrirConfiguracion());

        menuConfiguracion.add(itemConfiguracion);
        menuBar.add(menuConfiguracion);
    }

    private void crearMenuSistema(JMenuBar menuBar) {
        menuSistema = new JMenu("Sistema");

        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        JMenuItem itemSalir = new JMenuItem("Salir del Sistema");

        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        itemSalir.addActionListener(e -> confirmarSalida());

        menuSistema.add(itemCerrarSesion);
        menuSistema.addSeparator();
        menuSistema.add(itemSalir);
        menuBar.add(menuSistema);
    }

    private void configurarEstiloMenu() {

        menuBar.setBackground(
                EstiloUI.BLANCO
        );

        menuBar.setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        0,
                        2,
                        0,
                        EstiloUI.AZUL_MARINO
                )
        );

        estilizarMenu(menuProductos);
        estilizarMenu(menuClientes);
        estilizarMenu(menuUsuarios);
        estilizarMenu(menuVentas);
        estilizarMenu(menuConfiguracion);
        estilizarMenu(menuSistema);
    }

    private void estilizarMenu(
            JMenu menu) {

        menu.setForeground(
                EstiloUI.AZUL_MARINO
        );

        menu.setFont(
                menu.getFont().deriveFont(
                        Font.BOLD,
                        13f
                )
        );

        menu.setBorder(
                BorderFactory.createEmptyBorder(
                        7,
                        12,
                        7,
                        12
                )
        );

        for (Component componente :
                menu.getMenuComponents()) {

            if (componente
                    instanceof JMenuItem item) {

                item.setBackground(
                        EstiloUI.BLANCO
                );

                item.setForeground(
                        EstiloUI.TEXTO
                );

                item.setFont(
                        item.getFont().deriveFont(
                                13f
                        )
                );

                item.setBorder(
                        BorderFactory.createEmptyBorder(
                                7,
                                12,
                                7,
                                18
                        )
                );
            }
        }
    }

    private void mostrarUsuario() {

        Usuario usuario =
                sesionActiva.getUsuarioActual();

        lblBienvenida.setText(
                "Bienvenido, "
                        + usuario.getNombre()
                        + " "
                        + usuario.getApellido()
        );

        String rolVisible =
                usuario.isAdmin()
                        ? "Administrador"
                        : "Cajero";

        lblRol.setText(
                "Rol: " + rolVisible
        );
    }

    private void aplicarPermisos() {
        boolean esAdmin = sesionActiva.esAdmin();
        menuProductos.setVisible(esAdmin);
        menuUsuarios.setVisible(esAdmin);
        menuConfiguracion.setVisible(esAdmin);
    }

    private void abrirClientes() {
        ClienteView existente = buscarFrame(ClienteView.class);
        if (existente != null) {
            traerAlFrente(existente);
            return;
        }

        abrirFrame(new ClienteView(clienteService));
    }

    private void abrirProductos() {
        ProductoView existente = buscarFrame(ProductoView.class);
        if (existente != null) {
            traerAlFrente(existente);
            return;
        }

        abrirFrame(new ProductoView(productoService, configuracionService));
    }

    private void abrirUsuarios() {
        if (!sesionActiva.esAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No tiene permisos para acceder a la gestión de usuarios.",
                    "Acceso restringido",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        UsuarioView existente = buscarFrame(UsuarioView.class);
        if (existente != null) {
            traerAlFrente(existente);
            return;
        }

        abrirFrame(new UsuarioView(usuarioService));
    }

    private void abrirHistorialVentas() {
        HistorialVentasView existente = buscarFrame(HistorialVentasView.class);
        if (existente != null) {
            traerAlFrente(existente);
            return;
        }

        abrirFrame(new HistorialVentasView(ventaService, reporteFacturaService, sesionActiva));
    }

    private void abrirConfiguracion() {
        if (!sesionActiva.esAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No tiene permisos para acceder a la configuración.",
                    "Acceso restringido",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ConfiguracionView existente = buscarFrame(ConfiguracionView.class);
        if (existente != null) {
            traerAlFrente(existente);
            return;
        }

        abrirFrame(new ConfiguracionView(configuracionService));
    }

    private void abrirNuevaVenta() {
        VentaView existente = buscarFrame(VentaView.class);
        if (existente != null) {
            traerAlFrente(existente);
            return;
        }

        abrirFrame(new VentaView(productoService, clienteService, ventaService, reporteFacturaService));
    }

    private void abrirFrame(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
        if (frame.getWidth() <= 0 || frame.getHeight() <= 0) {
            frame.pack();
        }
        centrarFrame(frame);
        traerAlFrente(frame);
    }

    private void centrarFrame(JInternalFrame frame) {
        int x = Math.max(0, (desktopPane.getWidth() - frame.getWidth()) / 2);
        int y = Math.max(50, (desktopPane.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private <T extends JInternalFrame> T buscarFrame(Class<T> tipo) {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (tipo.isInstance(frame)) {
                return tipo.cast(frame);
            }
        }
        return null;
    }

    private void traerAlFrente(JInternalFrame frame) {
        try {
            if (frame.isIcon()) {
                frame.setIcon(false);
            }
            frame.setSelected(true);
            frame.toFront();
        } catch (java.beans.PropertyVetoException e) {
            frame.toFront();
        }
    }

    private void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea cerrar la sesión actual?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        autenticacionService.cerrarSesion();
        dispose();
        alCerrarSesion.run();
    }

    private void confirmarSalida() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea salir del sistema?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
