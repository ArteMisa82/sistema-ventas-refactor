package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.model.Cliente;
import ec.edu.uta.ventas.service.ClienteService;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClienteView extends JInternalFrame {
    private final ClienteService clienteService;

    private Integer clienteSeleccionadoId;
    private boolean modoEdicion = false;
    private List<Cliente> clientesMostrados;

    private final JTextField txtCedula;
    private final JTextField txtNombre;
    private final JTextField txtSegundoNombre;
    private final JTextField txtApellido;
    private final JTextField txtSegundoApellido;
    private final JTextField txtDireccion;
    private final JTextField txtTelefono;
    private final JTextField txtEmail;

    private final JTextField txtBuscar;
    private final JComboBox<String> cmbEstado;
    private final JButton btnLimpiar;
    private final JButton btnNuevo;
    private final JButton btnGuardar;
    private final JButton btnEditar;
    private final JButton btnDesactivar;
    private final JButton btnReactivar;
    private final DefaultTableModel tableModel;
    private final JTable tblClientes;

    private boolean mostrandoActivos() {
        return cmbEstado.getSelectedIndex() == 0;
    }

    public ClienteView(ClienteService clienteService) {
        this.clienteService = clienteService;

        setTitle("Gestión de Clientes");
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(EstiloUI.FONDO);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBackground(EstiloUI.BLANCO);
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(EstiloUI.TEXTO);

        txtBuscar = new JTextField(25);
        cmbEstado = new JComboBox<>(new String[]{"Activos", "Inactivos"});
        btnLimpiar = new JButton("Limpiar");

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setForeground(EstiloUI.TEXTO);
        panelBusqueda.add(lblEstado);
        panelBusqueda.add(cmbEstado);
        panelBusqueda.add(btnLimpiar);

        txtCedula = new JTextField(15);
        txtNombre = new JTextField(15);
        txtSegundoNombre = new JTextField(15);
        txtApellido = new JTextField(15);
        txtSegundoApellido = new JTextField(15);
        txtDireccion = new JTextField(20);
        txtTelefono = new JTextField(15);
        txtEmail = new JTextField(20);

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar");
        btnDesactivar = new JButton("Desactivar");
        btnReactivar = new JButton("Reactivar");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Cédula", "Nombre", "Apellido", "Teléfono", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblClientes = new JTable(tableModel);
        tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tblClientes);
        EstiloUI.scroll(scrollPane);
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBackground(EstiloUI.FONDO);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        panelCentral.add(crearPanelFormulario(), BorderLayout.NORTH);
        panelCentral.add(scrollPane, BorderLayout.CENTER);
        panelCentral.add(crearPanelBotones(), BorderLayout.SOUTH);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(EstiloUI.BLANCO);

        JLabel lblTitulo = new JLabel("GESTIÓN DE CLIENTES");
        EstiloUI.titulo(lblTitulo);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelBusqueda, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);

        configurarEstilos();
        configurarEventos();
        cargarClientes();
        limpiarFormulario();

        configurarEventos();
        cargarClientes();
        limpiarFormulario();
    }

    private void configurarEstilos() {
        EstiloUI.campo(txtCedula);
        EstiloUI.campo(txtNombre);
        EstiloUI.campo(txtSegundoNombre);
        EstiloUI.campo(txtApellido);
        EstiloUI.campo(txtSegundoApellido);
        EstiloUI.campo(txtDireccion);
        EstiloUI.campo(txtTelefono);
        EstiloUI.campo(txtEmail);
        EstiloUI.campo(txtBuscar);

        cmbEstado.setBackground(EstiloUI.BLANCO);
        cmbEstado.setForeground(EstiloUI.TEXTO);

        EstiloUI.tabla(tblClientes);
        }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridLayout(4, 4, 10, 10));
        panel.setBackground(EstiloUI.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Datos del cliente"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        agregarCampo(panel, "Cédula:", txtCedula);
        agregarCampo(panel, "Nombre:", txtNombre);
        agregarCampo(panel, "Segundo nombre:", txtSegundoNombre);
        agregarCampo(panel, "Apellido:", txtApellido);
        agregarCampo(panel, "Segundo apellido:", txtSegundoApellido);
        agregarCampo(panel, "Dirección:", txtDireccion);
        agregarCampo(panel, "Teléfono:", txtTelefono);
        agregarCampo(panel, "Email:", txtEmail);

        tblClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tblClientes.getColumnModel().getColumn(0).setMaxWidth(0);
        tblClientes.getColumnModel().getColumn(0).setPreferredWidth(0);

        return panel;
    }

    

    private void agregarCampo(JPanel panel, String texto, JTextField campo) {
        JLabel label = new JLabel(texto);
        label.setForeground(EstiloUI.TEXTO);
        panel.add(label);
        panel.add(campo);
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(btnNuevo);
        panel.add(btnGuardar);
        panel.add(btnEditar);
        panel.add(btnDesactivar);
        panel.add(btnReactivar);
        return panel;
    }

    private void configurarEventos() {
        cmbEstado.addActionListener(e -> {
            limpiarFormulario();
            cargarClientes();
        });

        btnReactivar.addActionListener(e -> reactivarCliente());

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                buscarClientes();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                buscarClientes();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                buscarClientes();
            }
        });

        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            limpiarFormulario();
            cargarClientes();
        });

        btnNuevo.addActionListener(e -> {
            if (!mostrandoActivos()) {
                cmbEstado.setSelectedIndex(0);
            }
            limpiarFormulario();
        });

        btnGuardar.addActionListener(e -> guardarCliente());
        btnEditar.addActionListener(e -> {
                if (modoEdicion) {
                        editarCliente();
                } else {
                        activarModoEdicion();
                }
        });
        btnDesactivar.addActionListener(e -> desactivarCliente());

        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarCliente();
            }
        });
    }

    private void activarModoEdicion() {
        if (clienteSeleccionadoId == null) {
                mostrarAdvertencia("Seleccione un cliente.");
                return;
        }

        modoEdicion = true;
        actualizarEstadoBotones();
        txtNombre.requestFocus();
        }

    private void cargarClientes() {
        try {
            List<Cliente> clientes;

            if (mostrandoActivos()) {
                clientes = clienteService.listarActivos();
            } else {
                clientes = clienteService.listarInactivos();
            }

            mostrarClientes(clientes);
            actualizarEstadoBotones();
        } catch (RuntimeException e) {
            mostrarError("No se pudieron cargar los clientes.");
        }
    }

    private void buscarClientes() {
        try {
            String texto = txtBuscar.getText().trim().toLowerCase();

            if (mostrandoActivos()) {
                List<Cliente> clientes = clienteService.buscar(texto);
                mostrarClientes(clientes);
                return;
            }

            List<Cliente> clientes = clienteService.listarInactivos();

            if (texto.isBlank()) {
                mostrarClientes(clientes);
                return;
            }

            List<Cliente> resultado = clientes.stream()
                    .filter(cliente -> coincideBusqueda(cliente, texto))
                    .toList();

            mostrarClientes(resultado);
        } catch (RuntimeException e) {
            mostrarError("No se pudo realizar la búsqueda.");
        }
    }

    private boolean coincideBusqueda(Cliente cliente, String texto) {
        return contiene(cliente.getCedula(), texto)
                || contiene(cliente.getNombre(), texto)
                || contiene(cliente.getApellido(), texto);
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }

    private void reactivarCliente() {
        if (clienteSeleccionadoId == null) {
            mostrarAdvertencia("Seleccione un cliente inactivo.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea reactivar el cliente?",
                "Confirmar reactivación",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            clienteService.reactivar(clienteSeleccionadoId);
            mostrarMensaje("Cliente reactivado correctamente.");

            limpiarFormulario();
            cargarClientes();
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError("No se pudo reactivar el cliente.");
        }
    }

    private void mostrarClientes(List<Cliente> clientes) {
        clientesMostrados = clientes;
        tableModel.setRowCount(0);

        for (Cliente cliente : clientes) {
            tableModel.addRow(new Object[]{
                    cliente.getId(),
                    cliente.getCedula(),
                    cliente.getNombre(),
                    cliente.getApellido(),
                    cliente.getTelefono(),
                    cliente.getEmail()
            });
        }
    }

    private void seleccionarCliente() {
        int fila = tblClientes.getSelectedRow();

        if (fila == -1 || clientesMostrados == null) {
            return;
        }

        int id = (int) tableModel.getValueAt(fila, 0);
        clientesMostrados.stream()
                .filter(cliente -> cliente.getId() == id)
                .findFirst()
                .ifPresent(this::mostrarCliente);
    }

    private void mostrarCliente(Cliente cliente) {
        clienteSeleccionadoId = cliente.getId();

        txtCedula.setText(cliente.getCedula());
        txtNombre.setText(cliente.getNombre());
        txtSegundoNombre.setText(cliente.getSegundoNombre());
        txtApellido.setText(cliente.getApellido());
        txtSegundoApellido.setText(cliente.getSegundoApellido());
        txtDireccion.setText(cliente.getDireccion());
        txtTelefono.setText(cliente.getTelefono());
        txtEmail.setText(cliente.getEmail());

        modoEdicion = false;
        actualizarEstadoBotones();
    }

    private Cliente obtenerClienteFormulario() {
        Cliente cliente = new Cliente();

        if (clienteSeleccionadoId != null) {
            cliente.setId(clienteSeleccionadoId);
        }

        cliente.setCedula(txtCedula.getText().trim());
        cliente.setNombre(txtNombre.getText().trim());
        cliente.setSegundoNombre(txtSegundoNombre.getText().trim());
        cliente.setApellido(txtApellido.getText().trim());
        cliente.setSegundoApellido(txtSegundoApellido.getText().trim());
        cliente.setDireccion(txtDireccion.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setEmail(txtEmail.getText().trim());

        return cliente;
    }

    private void guardarCliente() {
        try {
            Cliente cliente = obtenerClienteFormulario();
            clienteService.guardar(cliente);

            mostrarMensaje("Cliente guardado correctamente.");
            limpiarFormulario();
            cargarClientes();
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError("No se pudo guardar el cliente.\nDetalle: " + e.getMessage());
        }
    }

    private void editarCliente() {
        if (clienteSeleccionadoId == null) {
            mostrarAdvertencia("Seleccione un cliente.");
            return;
        }

        try {
            Cliente cliente = obtenerClienteFormulario();
            clienteService.actualizar(cliente);

            mostrarMensaje("Cliente actualizado correctamente.");
            limpiarFormulario();
            cargarClientes();
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError("No se pudo actualizar el cliente.");
        }
    }

    private void desactivarCliente() {
        if (clienteSeleccionadoId == null) {
            mostrarAdvertencia("Seleccione un cliente.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea desactivar el cliente?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            clienteService.desactivar(clienteSeleccionadoId);

            mostrarMensaje("Cliente desactivado correctamente.");
            limpiarFormulario();
            cargarClientes();
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError("No se pudo desactivar el cliente.");
        }
    }

    private void limpiarFormulario() {
        clienteSeleccionadoId = null;
        modoEdicion = false;

        txtCedula.setText("");
        txtNombre.setText("");
        txtSegundoNombre.setText("");
        txtApellido.setText("");
        txtSegundoApellido.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");

        tblClientes.clearSelection();
        actualizarEstadoBotones();
        txtCedula.requestFocus();
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void actualizarEstadoBotones() {
        boolean activos = mostrandoActivos();
        boolean seleccionado = clienteSeleccionadoId != null;

        btnGuardar.setEnabled(activos && !seleccionado);
        btnEditar.setEnabled(activos && seleccionado);
        btnDesactivar.setEnabled(activos && seleccionado && !modoEdicion);
        btnReactivar.setEnabled(!activos && seleccionado);

        btnEditar.setText(modoEdicion ? "Actualizar" : "Editar");

        boolean editable = activos && (!seleccionado || modoEdicion);

        txtCedula.setEditable(editable);
        txtNombre.setEditable(editable);
        txtSegundoNombre.setEditable(editable);
        txtApellido.setEditable(editable);
        txtSegundoApellido.setEditable(editable);
        txtDireccion.setEditable(editable);
        txtTelefono.setEditable(editable);
        txtEmail.setEditable(editable);
    }
}
