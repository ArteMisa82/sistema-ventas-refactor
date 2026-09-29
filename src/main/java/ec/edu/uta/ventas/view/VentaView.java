package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.model.Cliente;
import ec.edu.uta.ventas.model.DetalleVenta;
import ec.edu.uta.ventas.model.Producto;
import ec.edu.uta.ventas.model.Venta;
import ec.edu.uta.ventas.service.ClienteService;
import ec.edu.uta.ventas.service.ProductoService;
import ec.edu.uta.ventas.service.ReporteFacturaService;
import ec.edu.uta.ventas.service.VentaService;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaView extends JInternalFrame {

    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final VentaService ventaService;
    private final ReporteFacturaService reporteFacturaService;

    private final JRadioButton rbConsumidorFinal = new JRadioButton("Consumidor Final", true);
    private final JRadioButton rbClienteRegistrado = new JRadioButton("Cliente registrado");
    private final JTextField txtCedula = new JTextField(12);
    private final JTextField txtCliente = new JTextField(25);
    private final JButton btnBuscarCliente = new JButton("Buscar");
    private Cliente clienteSeleccionado;

    private final JTextField txtBuscarProducto = new JTextField();
    private final JTextField txtCodigoBarras = new JTextField();
    private final JSpinner spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JButton btnAgregar = new JButton("Agregar");

    private final DefaultTableModel modeloProductos = new DefaultTableModel(
            new Object[]{"Código", "Código barras", "Producto", "Precio", "Stock"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tablaProductos = new JTable(modeloProductos);
    private List<Producto> productosMostrados = new ArrayList<>();
    private Producto productoSeleccionado;

    private final DefaultTableModel modeloDetalle = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Cantidad", "Precio", "Subtotal"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tablaDetalle = new JTable(modeloDetalle);
    private final List<DetalleVenta> detalles = new ArrayList<>();
    private final JButton btnQuitar = new JButton("Quitar producto");

    private final JLabel lblSubtotal = new JLabel("$ 0.00");
    private final JLabel lblIva = new JLabel("Se calculará al guardar");
    private final JLabel lblTotal = new JLabel("Se calculará al guardar");
    private final JLabel lblFactura = new JLabel("Se generará al guardar");

    private final JButton btnGuardar = new JButton("Registrar venta");
    private final JButton btnCancelar = new JButton("Cancelar");
    private Timer timerBusqueda;
    private boolean actualizandoSeleccion = false;

    public VentaView(ProductoService productoService, ClienteService clienteService,
                     VentaService ventaService, ReporteFacturaService reporteFacturaService) {
        super("Nueva Venta", true, true, true, true);

        this.productoService = productoService;
        this.clienteService = clienteService;
        this.ventaService = ventaService;
        this.reporteFacturaService = reporteFacturaService;

        configurarVentana();
        crearInterfaz();
        configurarTablas();
        configurarEventos();
        configurarBusqueda();
        configurarEstilos();

        nuevaVenta();
        cargarProductos();
    }

    private void configurarVentana() {
        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    }

    private void configurarEstilos() {
        EstiloUI.fondo(getContentPane());

        EstiloUI.campo(txtCedula);
        EstiloUI.campo(txtCliente);
        EstiloUI.campo(txtBuscarProducto);
        EstiloUI.campo(txtCodigoBarras);

        EstiloUI.botonPrincipal(btnAgregar);
        EstiloUI.botonPrincipal(btnBuscarCliente);
        EstiloUI.botonPrincipal(btnGuardar);

        EstiloUI.botonSecundario(btnQuitar);
        EstiloUI.botonSecundario(btnCancelar);

        EstiloUI.tabla(tablaProductos);
        EstiloUI.tabla(tablaDetalle);

        rbConsumidorFinal.setBackground(EstiloUI.BLANCO);
        rbClienteRegistrado.setBackground(EstiloUI.BLANCO);
        rbConsumidorFinal.setForeground(EstiloUI.TEXTO);
        rbClienteRegistrado.setForeground(EstiloUI.TEXTO);

        lblSubtotal.setForeground(EstiloUI.AZUL_MARINO);
        lblIva.setForeground(EstiloUI.AZUL_MARINO);
        lblTotal.setForeground(EstiloUI.AZUL_MARINO);
        lblFactura.setForeground(EstiloUI.AZUL_MARINO);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 18f));
        lblFactura.setFont(lblFactura.getFont().deriveFont(Font.BOLD));
    }

    private void crearInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = crearPanelSuperior();
        JPanel panelCentral = crearPanelCentral();
        JPanel panelInferior = crearPanelInferior();

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelSuperior() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(EstiloUI.BLANCO);
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JLabel titulo = new JLabel("REGISTRO DE VENTA");
        EstiloUI.titulo(titulo);

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(EstiloUI.BLANCO);
        panelTitulo.add(titulo, BorderLayout.WEST);

        JPanel factura = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        factura.setBackground(EstiloUI.BLANCO);
        factura.add(new JLabel("Factura:"));
        factura.add(lblFactura);
        panelTitulo.add(factura, BorderLayout.EAST);

        JPanel panelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelCliente.setBackground(EstiloUI.BLANCO);
        panelCliente.setBorder(BorderFactory.createTitledBorder("Cliente"));

        ButtonGroup grupoCliente = new ButtonGroup();
        grupoCliente.add(rbConsumidorFinal);
        grupoCliente.add(rbClienteRegistrado);

        txtCliente.setEditable(false);

        panelCliente.add(rbConsumidorFinal);
        panelCliente.add(rbClienteRegistrado);
        panelCliente.add(new JLabel("Cédula:"));
        panelCliente.add(txtCedula);
        panelCliente.add(btnBuscarCliente);
        panelCliente.add(new JLabel("Cliente:"));
        panelCliente.add(txtCliente);

        principal.add(panelTitulo, BorderLayout.NORTH);
        principal.add(panelCliente, BorderLayout.CENTER);
        return principal;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.setBackground(EstiloUI.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JPanel panelProductos = new JPanel(new BorderLayout(5, 5));
        panelProductos.setBackground(EstiloUI.BLANCO);
        panelProductos.setBorder(BorderFactory.createTitledBorder("Productos disponibles"));

        JPanel busqueda = new JPanel(new BorderLayout(8, 5));
        busqueda.setBackground(EstiloUI.BLANCO);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(EstiloUI.TEXTO);
        busqueda.add(lblBuscar, BorderLayout.WEST);
        busqueda.add(txtBuscarProducto, BorderLayout.CENTER);

        JPanel agregar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        agregar.setBackground(EstiloUI.BLANCO);
        agregar.add(new JLabel("Código barras:"));

        txtCodigoBarras.setPreferredSize(new Dimension(140, 28));
        agregar.add(txtCodigoBarras);
        agregar.add(new JLabel("Cantidad:"));
        agregar.add(spCantidad);
        agregar.add(btnAgregar);

        busqueda.add(agregar, BorderLayout.EAST);
        panelProductos.add(busqueda, BorderLayout.NORTH);

        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        EstiloUI.scroll(scrollProductos);
        panelProductos.add(scrollProductos, BorderLayout.CENTER);

        JPanel panelDetalle = new JPanel(new BorderLayout(5, 5));
        panelDetalle.setBackground(EstiloUI.BLANCO);
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Detalle de venta"));

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        EstiloUI.scroll(scrollDetalle);
        panelDetalle.add(scrollDetalle, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setBackground(EstiloUI.BLANCO);
        acciones.add(btnQuitar);
        panelDetalle.add(acciones, BorderLayout.SOUTH);

        panel.add(panelProductos);
        panel.add(panelDetalle);

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(EstiloUI.BLANCO);
        principal.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JPanel totales = new JPanel(new GridLayout(3, 2, 15, 6));
        totales.setBackground(EstiloUI.BLANCO);

        JLabel sub = new JLabel("Subtotal:");
        JLabel iva = new JLabel("IVA:");
        JLabel total = new JLabel("TOTAL:");

        sub.setForeground(EstiloUI.TEXTO);
        iva.setForeground(EstiloUI.TEXTO);
        total.setForeground(EstiloUI.AZUL_MARINO);
        total.setFont(total.getFont().deriveFont(Font.BOLD));

        totales.add(sub);
        totales.add(lblSubtotal);
        totales.add(iva);
        totales.add(lblIva);
        totales.add(total);
        totales.add(lblTotal);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        botones.setBackground(EstiloUI.BLANCO);
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        principal.add(totales, BorderLayout.WEST);
        principal.add(botones, BorderLayout.EAST);

        return principal;
    }

    private void configurarTablas() {
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarProducto();
            }
        });
    }

    private void configurarEventos() {
        rbConsumidorFinal.addActionListener(e -> usarConsumidorFinal());
        rbClienteRegistrado.addActionListener(e -> usarClienteRegistrado());
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        txtCedula.addActionListener(e -> buscarCliente());
        btnAgregar.addActionListener(e -> agregarProducto());
        txtCodigoBarras.addActionListener(e -> buscarPorCodigoBarras());
        btnQuitar.addActionListener(e -> quitarProducto());
        btnGuardar.addActionListener(e -> registrarVenta());
        btnCancelar.addActionListener(e -> cancelarVenta());
    }

    private void configurarBusqueda() {
        timerBusqueda = new Timer(400, e -> buscarProductos());
        timerBusqueda.setRepeats(false);

        txtBuscarProducto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reiniciarTimer();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reiniciarTimer();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reiniciarTimer();
            }
        });
    }

    private void reiniciarTimer() {
        if (!actualizandoSeleccion) {
            timerBusqueda.restart();
        }
    }

    private void cargarProductos() {
        try {
            productosMostrados = productoService.listarActivos();
            mostrarProductos();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscarProductos() {
        try {
            String texto = txtBuscarProducto.getText().trim();
            productosMostrados = productoService.buscar(texto);
            mostrarProductos();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarProductos() {
        modeloProductos.setRowCount(0);

        for (Producto producto : productosMostrados) {
            modeloProductos.addRow(new Object[]{
                    producto.getCodigo(),
                    producto.getCodigoBarras(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getStock()
            });
        }
    }

    private void seleccionarProducto() {
        int fila = tablaProductos.getSelectedRow();

        if (fila < 0 || fila >= productosMostrados.size()) {
            productoSeleccionado = null;
            return;
        }

        productoSeleccionado = productosMostrados.get(fila);
        actualizandoSeleccion = true;

        try {
            txtBuscarProducto.setText(productoSeleccionado.getCodigo());
            String codigoBarras = productoSeleccionado.getCodigoBarras();
            txtCodigoBarras.setText(codigoBarras != null ? codigoBarras : "");
        } finally {
            actualizandoSeleccion = false;
        }
    }

    private void buscarPorCodigoBarras() {
        String codigo = txtCodigoBarras.getText().trim();

        if (codigo.isBlank()) {
            return;
        }

        try {
            Optional<Producto> resultado = productoService.buscarPorCodigoBarras(codigo);

            if (resultado.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No existe un producto activo con ese código de barras.",
                        "Producto",
                        JOptionPane.WARNING_MESSAGE
                );
                txtCodigoBarras.selectAll();
                return;
            }

            productoSeleccionado = resultado.get();
            agregarProducto();

            txtCodigoBarras.setText("");
            txtCodigoBarras.requestFocus();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void agregarProducto() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un producto.",
                    "Venta",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int cantidad = (Integer) spCantidad.getValue();
        DetalleVenta existente = buscarDetalle(productoSeleccionado.getId());

        int cantidadActual = existente == null ? 0 : existente.getCantidad();
        int cantidadTotal = cantidadActual + cantidad;

        if (cantidadTotal > productoSeleccionado.getStock()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Stock insuficiente.\nDisponible: " + productoSeleccionado.getStock(),
                    "Stock",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (existente != null) {
            existente.setCantidad(cantidadTotal);
        } else {
            detalles.add(new DetalleVenta(productoSeleccionado, cantidad));
        }

        actualizarDetalle();
        actualizarSubtotal();

        spCantidad.setValue(1);
        productoSeleccionado = null;
        tablaProductos.clearSelection();

        actualizandoSeleccion = true;
        try {
            txtBuscarProducto.setText("");
            txtCodigoBarras.setText("");
        } finally {
            actualizandoSeleccion = false;
        }

        cargarProductos();
        txtCodigoBarras.requestFocusInWindow();
    }

    private DetalleVenta buscarDetalle(int idProducto) {
        for (DetalleVenta detalle : detalles) {
            if (detalle.getProducto().getId() == idProducto) {
                return detalle;
            }
        }
        return null;
    }

    private void actualizarDetalle() {
        modeloDetalle.setRowCount(0);

        for (DetalleVenta detalle : detalles) {
            modeloDetalle.addRow(new Object[]{
                    detalle.getProducto().getCodigo(),
                    detalle.getProducto().getNombre(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotalItem()
            });
        }
    }

    private void quitarProducto() {
        int fila = tablaDetalle.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un producto del detalle.",
                    "Venta",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        detalles.remove(fila);
        actualizarDetalle();
        actualizarSubtotal();
    }

    private void actualizarSubtotal() {
        if (detalles.isEmpty()) {
            lblSubtotal.setText("$ 0.00");
            lblIva.setText("$ 0.00");
            lblTotal.setText("$ 0.00");
            return;
        }

        try {
            Venta calculo = ventaService.calcularTotales(new ArrayList<>(detalles));
            lblSubtotal.setText(formatoDinero(calculo.getSubtotal()));
            lblIva.setText(formatoDinero(calculo.getIva()) + " (" + calculo.getPorcentajeIva() + "%)");
            lblTotal.setText(formatoDinero(calculo.getTotal()));
        } catch (RuntimeException e) {
            mostrarError(obtenerMensajeError(e));
        }
    }

    private void usarConsumidorFinal() {
        clienteSeleccionado = null;
        txtCedula.setText("");
        txtCliente.setText("");
        actualizarEstadoCliente();
    }

    private void usarClienteRegistrado() {
        actualizarEstadoCliente();
        txtCedula.requestFocus();
    }

    private void actualizarEstadoCliente() {
        boolean registrado = rbClienteRegistrado.isSelected();
        txtCedula.setEnabled(registrado);
        btnBuscarCliente.setEnabled(registrado);

        if (!registrado) {
            clienteSeleccionado = null;
            txtCliente.setText("");
        }
    }

    private void buscarCliente() {
        String cedula = txtCedula.getText().trim();

        if (!cedula.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese una cédula de 10 dígitos.",
                    "Cliente",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Optional<Cliente> cliente = clienteService.buscarPorCedula(cedula);
            if (cliente.isPresent()) {
                seleccionarCliente(cliente.get());
                return;
            }

            verificarClienteInactivo(cedula);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void verificarClienteInactivo(String cedula) {
        Optional<Cliente> inactivo = clienteService.buscarInactivoPorCedula(cedula);

        if (inactivo.isPresent()) {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "El cliente está inactivo.\n¿Desea reactivarlo?",
                    "Cliente inactivo",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                Cliente cliente = inactivo.get();
                clienteService.reactivar(cliente.getId());
                seleccionarCliente(cliente);
            }
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "No existe un cliente registrado con esa cédula.",
                "Cliente",
                JOptionPane.INFORMATION_MESSAGE
        );

        clienteSeleccionado = null;
        txtCliente.setText("");
    }

    private void seleccionarCliente(Cliente cliente) {
        clienteSeleccionado = cliente;
        txtCliente.setText(nombreCliente(cliente));
    }

    private String nombreCliente(Cliente cliente) {
        StringBuilder nombre = new StringBuilder();
        agregarNombre(nombre, cliente.getNombre());
        agregarNombre(nombre, cliente.getSegundoNombre());
        agregarNombre(nombre, cliente.getApellido());
        agregarNombre(nombre, cliente.getSegundoApellido());
        return nombre.toString().trim();
    }

    private void agregarNombre(StringBuilder nombre, String parte) {
        if (parte == null || parte.isBlank()) {
            return;
        }

        if (!nombre.isEmpty()) {
            nombre.append(" ");
        }

        nombre.append(parte.trim());
    }

    private void registrarVenta() {
        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto.", "Venta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (rbClienteRegistrado.isSelected() && clienteSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente válido.", "Cliente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea registrar la venta?",
                "Confirmar venta",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Venta venta = ventaService.crearVenta(clienteSeleccionado, new ArrayList<>(detalles));

            lblFactura.setText(venta.getNumeroFactura());
            lblSubtotal.setText(formatoDinero(venta.getSubtotal()));
            lblIva.setText(formatoDinero(venta.getIva()) + " (" + venta.getPorcentajeIva() + "%)");
            lblTotal.setText(formatoDinero(venta.getTotal()));

            JOptionPane.showMessageDialog(
                    this,
                    "Venta registrada correctamente.\nFactura: " + venta.getNumeroFactura() + "\nTotal: " + formatoDinero(venta.getTotal()),
                    "Venta registrada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            int opcionFactura = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea visualizar la factura?",
                    "Factura",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcionFactura == JOptionPane.YES_OPTION) {
                try {
                    reporteFacturaService.mostrarFactura(venta.getId());
                } catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "La venta fue registrada correctamente, pero no se pudo generar la factura.\n" + obtenerMensajeError(e),
                            "Error al generar factura",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            nuevaVenta();
            cargarProductos();
        } catch (RuntimeException e) {
            mostrarError(obtenerMensajeError(e));
            cargarProductos();
        }
    }

    private void nuevaVenta() {
        detalles.clear();
        modeloDetalle.setRowCount(0);

        clienteSeleccionado = null;
        productoSeleccionado = null;
        rbConsumidorFinal.setSelected(true);

        txtCedula.setText("");
        txtCliente.setText("");
        txtBuscarProducto.setText("");
        txtCodigoBarras.setText("");
        spCantidad.setValue(1);

        lblFactura.setText("Se generará al guardar");
        lblSubtotal.setText("$ 0.00");
        lblIva.setText("$ 0.00");
        lblTotal.setText("$ 0.00");

        actualizarEstadoCliente();
    }

    private void cancelarVenta() {
        if (detalles.isEmpty()) {
            nuevaVenta();
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea cancelar la venta actual?",
                "Cancelar venta",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            nuevaVenta();
        }
    }

    private String formatoDinero(BigDecimal valor) {
        if (valor == null) {
            return "$ 0.00";
        }
        return "$ " + valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String obtenerMensajeError(Throwable error) {
        Throwable actual = error;

        while (actual != null) {
            if (actual.getMessage() != null && !actual.getMessage().isBlank()) {
                if (actual.getCause() == null) {
                    return actual.getMessage();
                }
            }
            actual = actual.getCause();
        }

        return "Ocurrió un error al procesar la venta.";
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
