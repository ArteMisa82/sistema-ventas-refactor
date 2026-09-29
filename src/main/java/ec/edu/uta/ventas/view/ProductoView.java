package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.model.Producto;
import ec.edu.uta.ventas.service.ConfiguracionService;
import ec.edu.uta.ventas.service.ProductoService;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductoView extends JInternalFrame {
    private final ProductoService productoService;
    private final ConfiguracionService configuracionService;

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtCodigoBarras = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JTextField txtBuscar = new JTextField();

    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Activos", "Inactivos"});
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnDesactivar = new JButton("Desactivar");
    private final JButton btnReactivar = new JButton("Reactivar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JLabel lblEstado = new JLabel("Nuevo producto");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Código", "Código de barras", "Nombre", "Precio", "Stock"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tablaProductos = new JTable(modeloTabla);
    private Timer timerBusqueda;
    private int idSeleccionado = -1;
    private boolean modoEdicion = false;
    private int stockMinimo;
    private List<Producto> productosMostrados = List.of();

    public ProductoView(ProductoService productoService, ConfiguracionService configuracionService) {
        this.productoService = productoService;
        this.configuracionService = configuracionService;

        configurarVentana();
        crearInterfaz();
        configurarTabla();
        configurarEventos();
        configurarBusqueda();

        cargarStockMinimo();
        configurarTablaStock();
        nuevoProducto();
        cargarProductos();
    }

    private void configurarVentana() {
        setTitle("Gestión de Productos");
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setSize(900, 620);
        setPreferredSize(new Dimension(900, 620));
    }

    private void crearInterfaz() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(EstiloUI.BLANCO);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("GESTIÓN DE PRODUCTOS", SwingConstants.CENTER);
        EstiloUI.titulo(lblTitulo);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(EstiloUI.BLANCO);
        panelFormulario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(EstiloUI.BORDE),
                "Datos del producto"
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(panelFormulario, gbc, 0, 0, "Código:", txtCodigo);
        agregarCampo(panelFormulario, gbc, 0, 1, "Código de barras:", txtCodigoBarras);
        agregarCampo(panelFormulario, gbc, 1, 0, "Nombre:", txtNombre);
        agregarCampo(panelFormulario, gbc, 1, 1, "Precio:", txtPrecio);
        agregarCampo(panelFormulario, gbc, 2, 0, "Stock:", txtStock);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblOpcional = new JLabel("Código de barras opcional");
        lblOpcional.setForeground(Color.GRAY);
        panelFormulario.add(lblOpcional, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        panelBotones.setBackground(EstiloUI.BLANCO);

        EstiloUI.botonPrincipal(btnNuevo);
        EstiloUI.botonPrincipal(btnGuardar);
        EstiloUI.botonPrincipal(btnEditar);
        EstiloUI.botonPrincipal(btnDesactivar);
        EstiloUI.botonPrincipal(btnReactivar);
        EstiloUI.botonSecundario(btnLimpiar);

        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnDesactivar);
        panelBotones.add(btnReactivar);

        lblEstado.setForeground(EstiloUI.AZUL_SECUNDARIO);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(EstiloUI.BLANCO);
        panelSuperior.add(lblTitulo);
        panelSuperior.add(panelFormulario);
        panelSuperior.add(Box.createVerticalStrut(5));
        panelSuperior.add(panelBotones);
        panelSuperior.add(lblEstado);

        JPanel panelBusqueda = new JPanel(new BorderLayout(8, 0));
        panelBusqueda.setBackground(EstiloUI.BLANCO);

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFiltro.setBackground(EstiloUI.BLANCO);
        panelFiltro.add(new JLabel("Estado:"));
        panelFiltro.add(cmbEstado);
        panelFiltro.add(btnLimpiar);

        panelBusqueda.add(new JLabel("Buscar:"), BorderLayout.WEST);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
        panelBusqueda.add(panelFiltro, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        EstiloUI.scroll(scrollPane);

        JPanel panelCentro = new JPanel(new BorderLayout(5, 8));
        panelCentro.setBackground(EstiloUI.BLANCO);
        panelCentro.add(panelBusqueda, BorderLayout.NORTH);
        panelCentro.add(scrollPane, BorderLayout.CENTER);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        setContentPane(panelPrincipal);

        configurarEstilos();
    }

    private void configurarEstilos() {
        EstiloUI.campo(txtCodigo);
        EstiloUI.campo(txtCodigoBarras);
        EstiloUI.campo(txtNombre);
        EstiloUI.campo(txtPrecio);
        EstiloUI.campo(txtStock);
        EstiloUI.campo(txtBuscar);
        EstiloUI.tabla(tablaProductos);

        cmbEstado.setBackground(EstiloUI.BLANCO);
        cmbEstado.setForeground(EstiloUI.TEXTO);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, int columna, String etiqueta, JTextField campo) {
        int columnaBase = columna * 2;

        gbc.gridx = columnaBase;
        gbc.gridy = fila;
        gbc.weightx = 0;
        JLabel label = new JLabel(etiqueta);
        label.setForeground(EstiloUI.TEXTO);
        panel.add(label, gbc);

        gbc.gridx = columnaBase + 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void configurarTabla() {
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(0);

        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarProductoSeleccionado();
            }
        });
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevoProducto());
        btnGuardar.addActionListener(e -> guardarProducto());
        btnEditar.addActionListener(e -> {
            if (modoEdicion) {
                editarProducto();
            } else {
                activarModoEdicion();
            }
        });
        btnDesactivar.addActionListener(e -> desactivarProducto());
        btnReactivar.addActionListener(e -> reactivarProducto());
        btnLimpiar.addActionListener(e -> limpiar());

        cmbEstado.addActionListener(e -> {
            limpiarFormulario();
            cargarProductos();
            actualizarEstadoBotones();
        });
    }

    private void activarModoEdicion() {
        if (idSeleccionado <= 0) {
            mostrarError("Seleccione un producto.");
            return;
        }

        modoEdicion = true;
        actualizarEstadoBotones();
        txtNombre.requestFocus();
    }

    private void configurarBusqueda() {
        timerBusqueda = new Timer(400, e -> cargarProductos());
        timerBusqueda.setRepeats(false);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            private void buscar() {
                timerBusqueda.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                buscar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                buscar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                buscar();
            }
        });
    }

    private void cargarProductos() {
        try {
            String texto = txtBuscar.getText().trim();

            if (mostrandoActivos()) {
                productosMostrados = productoService.buscar(texto);
            } else {
                productosMostrados = filtrarInactivos(texto);
            }


            llenarTabla(productosMostrados);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private List<Producto> filtrarInactivos(String texto) {
        List<Producto> productos = productoService.listarInactivos();

        if (texto == null || texto.isBlank()) {
            return productos;
        }

        String filtro = texto.trim().toLowerCase();
        return productos.stream()
                .filter(producto -> contiene(producto.getCodigo(), filtro)
                        || contiene(producto.getCodigoBarras(), filtro)
                        || contiene(producto.getNombre(), filtro))
                .toList();
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private void llenarTabla(List<Producto> productos) {
        modeloTabla.setRowCount(0);

        for (Producto producto : productos) {
            modeloTabla.addRow(new Object[]{
                    producto.getId(),
                    producto.getCodigo(),
                    producto.getCodigoBarras(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getStock()
            });
        }
    }

    private void cargarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();

        if (fila < 0 || fila >= productosMostrados.size()) {
            return;
        }

        Producto producto = productosMostrados.get(fila);
        idSeleccionado = producto.getId();

        txtCodigo.setText(producto.getCodigo());
        txtCodigoBarras.setText(producto.getCodigoBarras() == null ? "" : producto.getCodigoBarras());
        txtNombre.setText(producto.getNombre());
        txtPrecio.setText(producto.getPrecio().toPlainString());
        txtStock.setText(String.valueOf(producto.getStock()));

        modoEdicion = false;
        lblEstado.setText("Producto seleccionado: " + producto.getNombre());
        actualizarEstadoBotones();
    }

    private void nuevoProducto() {
        if (!mostrandoActivos()) {
            cmbEstado.setSelectedIndex(0);
        }

        limpiarFormulario();
        modoEdicion = false;

        try {
            txtCodigo.setText(productoService.obtenerSiguienteCodigo());
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }

        txtCodigo.setEditable(false);
        lblEstado.setText("Nuevo producto");
        actualizarEstadoBotones();
        txtNombre.requestFocus();
    }

    private void guardarProducto() {
        if (!mostrandoActivos()) {
            return;
        }

        try {
            BigDecimal precio = obtenerPrecio();
            int stock = obtenerStock();

            productoService.registrar(txtNombre.getText(), precio, stock, txtCodigoBarras.getText());
            JOptionPane.showMessageDialog(this, "Producto registrado correctamente.");

            limpiarFormulario();
            cargarProductos();
            nuevoProducto();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    private void editarProducto() {
        if (idSeleccionado <= 0) {
            mostrarError("Seleccione un producto.");
            return;
        }

        try {
            BigDecimal precio = obtenerPrecio();
            int stock = obtenerStock();

            productoService.actualizar(idSeleccionado, txtNombre.getText(), precio, stock, txtCodigoBarras.getText());
            JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");

            limpiarFormulario();
            cargarProductos();
            nuevoProducto();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    private void desactivarProducto() {
        if (idSeleccionado <= 0) {
            mostrarError("Seleccione un producto.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea desactivar el producto \"" + nombre + "\"?",
                "Confirmar desactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            productoService.desactivar(idSeleccionado);
            JOptionPane.showMessageDialog(this, "Producto desactivado correctamente.");

            limpiarFormulario();
            cargarProductos();
            nuevoProducto();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    private void reactivarProducto() {
        if (idSeleccionado <= 0) {
            mostrarError("Seleccione un producto inactivo.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea reactivar el producto \"" + nombre + "\"?",
                "Confirmar reactivación",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            productoService.reactivar(idSeleccionado);
            JOptionPane.showMessageDialog(this, "Producto reactivado correctamente.");

            limpiarFormulario();
            cargarProductos();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError(e.getMessage());
        }
    }

    private BigDecimal obtenerPrecio() {
        String texto = txtPrecio.getText().trim();

        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }

        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un número válido.");
        }
    }

    private int obtenerStock() {
        String texto = txtStock.getText().trim();

        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El stock es obligatorio.");
        }

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El stock debe ser un número entero.");
        }
    }

    private void configurarTablaStock() {
        tablaProductos.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    return component;
                }

                component.setForeground(EstiloUI.TEXTO);
                component.setBackground(EstiloUI.BLANCO);

                int columnaStock = buscarColumnaStock(table);
                if (columnaStock == -1) {
                    return component;
                }

                Object valorStock = table.getValueAt(row, columnaStock);
                if (valorStock instanceof Number numero) {
                    int stock = numero.intValue();
                    if (stock <= stockMinimo) {
                        component.setBackground(new Color(255, 220, 220));
                    }
                }

                return component;
            }
        });
    }

    private int buscarColumnaStock(JTable table) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if ("Stock".equalsIgnoreCase(table.getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }

    private void cargarStockMinimo() {
        try {
            stockMinimo = configuracionService.obtenerStockMinimo();
        } catch (RuntimeException e) {
            stockMinimo = 0;
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar el stock mínimo.\n" + e.getMessage(),
                    "Configuración",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void limpiar() {
        txtBuscar.setText("");
        limpiarFormulario();
        cargarProductos();

        if (mostrandoActivos()) {
            nuevoProducto();
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        modoEdicion = false;

        txtCodigo.setText("");
        txtCodigoBarras.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");

        tablaProductos.clearSelection();
        lblEstado.setText("Ningún producto seleccionado");
        actualizarEstadoBotones();
    }

    private boolean mostrandoActivos() {
        return cmbEstado.getSelectedIndex() == 0;
    }

    private void actualizarEstadoBotones() {
        boolean activos = mostrandoActivos();
        boolean seleccionado = idSeleccionado > 0;

        btnGuardar.setEnabled(activos && !seleccionado);
        btnEditar.setEnabled(activos && seleccionado);
        btnDesactivar.setEnabled(activos && seleccionado && !modoEdicion);
        btnReactivar.setEnabled(!activos && seleccionado);

        btnEditar.setText(modoEdicion ? "Actualizar" : "Editar");

        boolean editable = activos && (!seleccionado || modoEdicion);

        txtNombre.setEditable(editable);
        txtPrecio.setEditable(editable);
        txtStock.setEditable(editable);
        txtCodigoBarras.setEditable(editable);
        txtCodigo.setEditable(false);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
