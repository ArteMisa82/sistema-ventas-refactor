package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.model.DetalleVenta;
import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.model.Venta;
import ec.edu.uta.ventas.service.ReporteFacturaService;
import ec.edu.uta.ventas.service.VentaService;
import ec.edu.uta.ventas.session.SesionActiva;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HistorialVentasView extends JInternalFrame {
    private final VentaService ventaService;
    private final SesionActiva sesionActiva;
    private final ReporteFacturaService reporteFacturaService;

    private final JTextField txtBuscarFactura = new JTextField();
    private final JTextField txtFecha = new JTextField();
    private final JButton btnBuscarFecha = new JButton("Buscar fecha");
    private final JButton btnMostrarTodas = new JButton("Mostrar todas");
    private final JButton btnAnular = new JButton("Anular venta");
    private final JButton btnVerDetalle = new JButton("Ver detalle");
    private final JButton btnReimprimir = new JButton("Reimprimir factura");
    private final JLabel lblEstado = new JLabel("Historial de ventas");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Factura", "Fecha", "Cliente", "Usuario", "Subtotal", "IVA", "Total", "Estado"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tablaVentas = new JTable(modeloTabla);
    private List<Venta> ventasMostradas = new ArrayList<>();
    private Timer timerBusqueda;

    private final DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter formatoFechaBusqueda = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HistorialVentasView(VentaService ventaService, ReporteFacturaService reporteFacturaService, SesionActiva sesionActiva) {
        super("Historial de Ventas", true, true, true, true);

        this.ventaService = ventaService;
        this.reporteFacturaService = reporteFacturaService;
        this.sesionActiva = sesionActiva;

        configurarVentana();
        crearInterfaz();
        configurarTabla();
        configurarEventos();
        configurarBusqueda();
        configurarEstilos();
        aplicarPermisos();
        cargarVentas();
    }

    private void configurarVentana() {
        setSize(1100, 650);
        setMinimumSize(new Dimension(900, 550));
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    }

    private void crearInterfaz() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(EstiloUI.FONDO);

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBackground(EstiloUI.BLANCO);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JLabel titulo = new JLabel("HISTORIAL DE VENTAS");
        EstiloUI.titulo(titulo);

        JPanel filtros = new JPanel(new BorderLayout(10, 5));
        filtros.setBackground(EstiloUI.BLANCO);

        JPanel panelFactura = new JPanel(new BorderLayout(5, 5));
        panelFactura.setBackground(EstiloUI.BLANCO);
        panelFactura.add(new JLabel("Buscar por factura:"), BorderLayout.WEST);
        panelFactura.add(txtBuscarFactura, BorderLayout.CENTER);

        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panelFecha.setBackground(EstiloUI.BLANCO);
        panelFecha.add(new JLabel("Fecha (dd/MM/yyyy):"));
        txtFecha.setPreferredSize(new Dimension(120, 28));
        panelFecha.add(txtFecha);
        panelFecha.add(btnBuscarFecha);
        panelFecha.add(btnMostrarTodas);

        filtros.add(panelFactura, BorderLayout.NORTH);
        filtros.add(panelFecha, BorderLayout.SOUTH);

        panelSuperior.add(titulo, BorderLayout.NORTH);
        panelSuperior.add(filtros, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tablaVentas);
        scroll.setBorder(BorderFactory.createTitledBorder("Ventas registradas"));
        EstiloUI.scroll(scroll);
        add(scroll, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(EstiloUI.BLANCO);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        panelInferior.add(lblEstado, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        acciones.setBackground(EstiloUI.BLANCO);
        acciones.add(btnVerDetalle);
        acciones.add(btnReimprimir);
        acciones.add(btnAnular);

        panelInferior.add(acciones, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void configurarTabla() {
        tablaVentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVentas.setAutoCreateRowSorter(true);

        tablaVentas.getColumnModel().getColumn(0).setMinWidth(0);
        tablaVentas.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(0);
    }

    private void configurarEventos() {
        btnBuscarFecha.addActionListener(e -> buscarPorFecha());
        txtFecha.addActionListener(e -> buscarPorFecha());

        btnMostrarTodas.addActionListener(e -> {
            txtBuscarFactura.setText("");
            txtFecha.setText("");
            cargarVentas();
        });

        btnAnular.addActionListener(e -> anularVenta());
        btnVerDetalle.addActionListener(e -> verDetalleVenta());
        btnReimprimir.addActionListener(e -> reimprimirFactura());
    }

    private void configurarBusqueda() {
        timerBusqueda = new Timer(400, e -> buscarPorFactura());
        timerBusqueda.setRepeats(false);

        txtBuscarFactura.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reiniciarBusqueda();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reiniciarBusqueda();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reiniciarBusqueda();
            }
        });
    }

    private void configurarEstilos() {
        EstiloUI.campo(txtBuscarFactura);
        EstiloUI.campo(txtFecha);

        EstiloUI.botonPrincipal(btnBuscarFecha);
        EstiloUI.botonSecundario(btnMostrarTodas);
        EstiloUI.botonPrincipal(btnVerDetalle);
        EstiloUI.botonSecundario(btnReimprimir);
        EstiloUI.botonSecundario(btnAnular);

        btnAnular.setBackground(EstiloUI.ROJO);
        btnAnular.setForeground(EstiloUI.BLANCO);

        EstiloUI.tabla(tablaVentas);

        lblEstado.setForeground(EstiloUI.AZUL_MARINO);
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD));
    }

    private void reiniciarBusqueda() {
        timerBusqueda.restart();
    }

    private void aplicarPermisos() {
        btnAnular.setVisible(sesionActiva.esAdmin());
    }

    private void cargarVentas() {
        try {
            ventasMostradas = new ArrayList<>(ventaService.listar());
            llenarTabla();
            lblEstado.setText("Ventas encontradas: " + ventasMostradas.size());
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void verDetalleVenta() {
        int filaVista = tablaVentas.getSelectedRow();

        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta para ver su detalle.", "Detalle de venta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaVentas.convertRowIndexToModel(filaVista);
        int idVenta = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String factura = String.valueOf(modeloTabla.getValueAt(filaModelo, 1));
        String cliente = String.valueOf(modeloTabla.getValueAt(filaModelo, 3));
        BigDecimal subtotal = (BigDecimal) modeloTabla.getValueAt(filaModelo, 5);
        BigDecimal iva = (BigDecimal) modeloTabla.getValueAt(filaModelo, 6);
        BigDecimal total = (BigDecimal) modeloTabla.getValueAt(filaModelo, 7);

        try {
            List<DetalleVenta> detalles = ventaService.listarDetalles(idVenta);
            mostrarDetalle(factura, cliente, subtotal, iva, total, detalles);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarDetalle(String factura, String cliente, BigDecimal subtotal, BigDecimal iva, BigDecimal total, List<DetalleVenta> detalles) {
        DefaultTableModel modeloDetalle = new DefaultTableModel(
                new Object[]{"Código", "Producto", "Cantidad", "Precio", "Subtotal"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (DetalleVenta detalle : detalles) {
            modeloDetalle.addRow(new Object[]{
                    detalle.getProducto().getCodigo(),
                    detalle.getProducto().getNombre(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotalItem()
            });
        }

        JTable tablaDetalle = new JTable(modeloDetalle);
        tablaDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        EstiloUI.tabla(tablaDetalle);

        JScrollPane scroll = new JScrollPane(tablaDetalle);
        scroll.setPreferredSize(new Dimension(650, 250));
        EstiloUI.scroll(scroll);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(EstiloUI.BLANCO);

        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBackground(EstiloUI.BLANCO);

        JLabel lblFacturaDetalle = new JLabel("Factura: " + factura);
        JLabel lblClienteDetalle = new JLabel("Cliente: " + cliente);
        lblFacturaDetalle.setForeground(EstiloUI.AZUL_MARINO);
        lblFacturaDetalle.setFont(lblFacturaDetalle.getFont().deriveFont(Font.BOLD));

        cabecera.add(lblFacturaDetalle);
        cabecera.add(lblClienteDetalle);

        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel totales = new JPanel();
        totales.setLayout(new BoxLayout(totales, BoxLayout.Y_AXIS));
        totales.setBackground(EstiloUI.BLANCO);

        JLabel lblSub = new JLabel("Subtotal: $" + subtotal);
        JLabel lblIvaDetalle = new JLabel("IVA: $" + iva);
        JLabel lblTotalDetalle = new JLabel("Total: $" + total);
        lblTotalDetalle.setForeground(EstiloUI.AZUL_MARINO);
        lblTotalDetalle.setFont(lblTotalDetalle.getFont().deriveFont(Font.BOLD));

        totales.add(lblSub);
        totales.add(lblIvaDetalle);
        totales.add(lblTotalDetalle);

        panel.add(totales, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Detalle de " + factura, JOptionPane.PLAIN_MESSAGE);
    }

    private void buscarPorFactura() {
        String factura = txtBuscarFactura.getText().trim();

        if (factura.isBlank()) {
            cargarVentas();
            return;
        }

        try {
            Optional<Venta> resultado = ventaService.buscarPorNumeroFactura(factura);
            ventasMostradas = new ArrayList<>();
            resultado.ifPresent(ventasMostradas::add);
            llenarTabla();

            if (ventasMostradas.isEmpty()) {
                lblEstado.setText("No se encontró la factura.");
            } else {
                lblEstado.setText("Factura encontrada.");
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscarPorFecha() {
        String texto = txtFecha.getText().trim();

        if (texto.isBlank()) {
            mostrarError("Ingrese una fecha.");
            return;
        }

        try {
            LocalDate fecha = LocalDate.parse(texto, formatoFechaBusqueda);
            ventasMostradas = new ArrayList<>(ventaService.buscarPorFecha(fecha));
            llenarTabla();
            lblEstado.setText("Ventas encontradas: " + ventasMostradas.size());
        } catch (DateTimeParseException e) {
            mostrarError("La fecha debe tener el formato dd/MM/yyyy.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void llenarTabla() {
        modeloTabla.setRowCount(0);

        for (Venta venta : ventasMostradas) {
            Usuario usuario = venta.getUsuario();
            String nombreUsuario = usuario == null ? "" : obtenerNombreUsuario(usuario);
            String fecha = venta.getFecha() == null ? "" : venta.getFecha().format(formatoFechaHora);

            modeloTabla.addRow(new Object[]{
                    venta.getId(),
                    venta.getNumeroFactura(),
                    fecha,
                    venta.getCliente(),
                    nombreUsuario,
                    venta.getSubtotal(),
                    venta.getIva(),
                    venta.getTotal(),
                    venta.isAnulada() ? "ANULADA" : "COMPLETADA"
            });
        }
    }

    private String obtenerNombreUsuario(Usuario usuario) {
        String nombre = usuario.getNombre();
        if (nombre != null && !nombre.isBlank()) {
            return nombre;
        }

        String username = usuario.getUsername();
        return username == null ? "" : username;
    }

    private void anularVenta() {
        if (!sesionActiva.esAdmin()) {
            mostrarError("Solo un administrador puede anular ventas.");
            return;
        }

        int filaVista = tablaVentas.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta.", "Anular venta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaVentas.convertRowIndexToModel(filaVista);
        int idVenta = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String factura = String.valueOf(modeloTabla.getValueAt(filaModelo, 1));
        String estado = String.valueOf(modeloTabla.getValueAt(filaModelo, 8));

        if ("ANULADA".equals(estado)) {
            JOptionPane.showMessageDialog(this, "La venta ya se encuentra anulada.", "Anular venta", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea anular la venta " + factura + "?\n\nEl stock de los productos será devuelto.", "Confirmar anulación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ventaService.anular(idVenta);
            JOptionPane.showMessageDialog(this, "Venta anulada correctamente.\nFactura: " + factura, "Venta anulada", JOptionPane.INFORMATION_MESSAGE);
            cargarVentas();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje == null ? "Ocurrió un error." : mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void reimprimirFactura() {
        int filaVista = tablaVentas.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta para reimprimir la factura.", "Reimprimir factura", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaVentas.convertRowIndexToModel(filaVista);
        int idVenta = (Integer) modeloTabla.getValueAt(filaModelo, 0);

        try {
            reporteFacturaService.mostrarFactura(idVenta);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error al generar factura", JOptionPane.ERROR_MESSAGE);
        }
    }
}