package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.service.UsuarioService;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioView extends JInternalFrame {
    private final UsuarioService usuarioService;

    private final JTextField txtBuscar = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtApellido = new JTextField();
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    private final JComboBox<String> cmbRol = new JComboBox<>(new String[]{"ADMIN", "CAJERO"});
    private final JCheckBox chkActivo = new JCheckBox("Usuario activo", true);

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnDesactivar = new JButton("Desactivar");
    private final JButton btnCambiarPassword = new JButton("Cambiar contraseña");
    private final JButton btnLimpiar = new JButton("Limpiar");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Apellido", "Usuario", "Rol", "Activo"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tablaUsuarios = new JTable(modeloTabla);
    private final Timer timerBusqueda;

    private List<Usuario> usuariosMostrados = new ArrayList<>();
    private Usuario usuarioSeleccionado;
    private boolean modoEdicion;

    public UsuarioView(UsuarioService usuarioService) {
        super("Administración de Usuarios", true, true, true, true);
        this.usuarioService = usuarioService;

        setSize(1000, 650);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        timerBusqueda = new Timer(400, e -> buscarUsuarios());
        timerBusqueda.setRepeats(false);

        inicializarComponentes();
        configurarEventos();
        cargarUsuarios();
        nuevoUsuario();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(EstiloUI.FONDO);

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBackground(EstiloUI.BLANCO);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel lblTitulo = new JLabel("GESTIÓN DE USUARIOS");
        EstiloUI.titulo(lblTitulo);

        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setBackground(EstiloUI.BLANCO);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(EstiloUI.TEXTO);

        panelBusqueda.add(lblBuscar, BorderLayout.WEST);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelBusqueda, BorderLayout.SOUTH);
        add(panelSuperior, BorderLayout.NORTH);

        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        EstiloUI.scroll(scrollPane);

        JPanel panelFormulario = crearPanelFormulario();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, panelFormulario);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(620);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        splitPane.setBackground(EstiloUI.FONDO);

        add(splitPane, BorderLayout.CENTER);
        configurarEstilos();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EstiloUI.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Datos del usuario"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int fila = 0;
        agregarCampo(panel, gbc, fila++, "Nombre:", txtNombre);
        agregarCampo(panel, gbc, fila++, "Apellido:", txtApellido);
        agregarCampo(panel, gbc, fila++, "Usuario:", txtUsername);
        agregarCampo(panel, gbc, fila++, "Contraseña:", txtPassword);
        agregarCampo(panel, gbc, fila++, "Rol:", cmbRol);

        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        chkActivo.setBackground(EstiloUI.BLANCO);
        chkActivo.setForeground(EstiloUI.TEXTO);
        panel.add(chkActivo, gbc);

        JPanel panelBotones = new JPanel(new GridLayout(3, 2, 8, 8));
        panelBotones.setBackground(EstiloUI.BLANCO);

        EstiloUI.botonSecundario(btnNuevo);
        EstiloUI.botonPrincipal(btnGuardar);
        EstiloUI.botonPrincipal(btnEditar);
        EstiloUI.botonSecundario(btnDesactivar);
        EstiloUI.botonSecundario(btnCambiarPassword);
        EstiloUI.botonSecundario(btnLimpiar);

        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnDesactivar);
        panelBotones.add(btnCambiarPassword);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, Component componente) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        JLabel label = new JLabel(etiqueta);
        label.setForeground(EstiloUI.TEXTO);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(componente, gbc);
    }

    private void configurarEstilos() {
        EstiloUI.campo(txtBuscar);
        EstiloUI.campo(txtNombre);
        EstiloUI.campo(txtApellido);
        EstiloUI.campo(txtUsername);
        EstiloUI.campoPassword(txtPassword);
        EstiloUI.tabla(tablaUsuarios);

        cmbRol.setBackground(EstiloUI.BLANCO);
        cmbRol.setForeground(EstiloUI.TEXTO);
    }

    private void configurarEventos() {
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
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

        DocumentListener generarUsuarioListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                generarUsername();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                generarUsername();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                generarUsername();
            }
        };

        txtNombre.getDocument().addDocumentListener(generarUsuarioListener);
        txtApellido.getDocument().addDocumentListener(generarUsuarioListener);

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarUsuario();
            }
        });

        btnNuevo.addActionListener(e -> nuevoUsuario());
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnEditar.addActionListener(e -> editarUsuario());
        btnDesactivar.addActionListener(e -> desactivarUsuario());
        btnCambiarPassword.addActionListener(e -> cambiarPassword());

        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarUsuarios();
            nuevoUsuario();
        });
    }

    private void generarUsername() {
        if (usuarioSeleccionado != null) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();

        if (nombre.isBlank() || apellido.isBlank()) {
            txtUsername.setText("");
            return;
        }

        String primerNombre = nombre.split("\\s+")[0];
        String primerApellido = apellido.split("\\s+")[0];

        String username = primerNombre.substring(0, 1).toLowerCase() + primerApellido.toLowerCase();
        txtUsername.setText(username.replaceAll("[^a-z0-9]", ""));
    }

    private void reiniciarBusqueda() {
        timerBusqueda.restart();
    }

    private void cargarUsuarios() {
        try {
            usuariosMostrados = usuarioService.listarTodos();
            mostrarUsuarios(usuariosMostrados);
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void buscarUsuarios() {
        try {
            usuariosMostrados = usuarioService.buscar(txtBuscar.getText());
            mostrarUsuarios(usuariosMostrados);
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void mostrarUsuarios(List<Usuario> usuarios) {
        modeloTabla.setRowCount(0);

        for (Usuario usuario : usuarios) {
            modeloTabla.addRow(new Object[]{
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getUsername(),
                    usuario.getRol(),
                    usuario.isActivo() ? "Sí" : "No"
            });
        }
    }

    private void seleccionarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0 || fila >= usuariosMostrados.size()) {
            return;
        }

        usuarioSeleccionado = usuariosMostrados.get(fila);
        modoEdicion = false;

        txtNombre.setText(usuarioSeleccionado.getNombre());
        txtApellido.setText(usuarioSeleccionado.getApellido());
        txtUsername.setText(usuarioSeleccionado.getUsername());
        txtPassword.setText("");

        cmbRol.setSelectedItem(usuarioSeleccionado.getRol());
        chkActivo.setSelected(usuarioSeleccionado.isActivo());

        txtNombre.setEditable(false);
        txtApellido.setEditable(false);
        txtUsername.setEditable(false);
        txtPassword.setEnabled(false);
        cmbRol.setEnabled(false);
        chkActivo.setEnabled(false);

        btnGuardar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnEditar.setText("Editar");
        btnDesactivar.setEnabled(usuarioSeleccionado.isActivo() && !esAdminPrincipal(usuarioSeleccionado));
        btnCambiarPassword.setEnabled(true);
    }

    private void nuevoUsuario() {
        usuarioSeleccionado = null;
        modoEdicion = false;

        tablaUsuarios.clearSelection();
        txtNombre.setText("");
        txtApellido.setText("");
        txtUsername.setText("");
        txtPassword.setText("");

        cmbRol.setSelectedItem("CAJERO");
        chkActivo.setSelected(true);

        txtNombre.setEditable(true);
        txtApellido.setEditable(true);
        txtUsername.setEditable(false);
        txtPassword.setEnabled(true);
        cmbRol.setEnabled(true);
        chkActivo.setEnabled(false);

        btnGuardar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnEditar.setText("Editar");
        btnDesactivar.setEnabled(false);
        btnCambiarPassword.setEnabled(false);

        txtNombre.requestFocusInWindow();
    }

    private void guardarUsuario() {
        try {
            String password = new String(txtPassword.getPassword());
            usuarioService.registrar(
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtUsername.getText(),
                    password,
                    (String) cmbRol.getSelectedItem()
            );

            JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.", "Usuarios", JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios();
            nuevoUsuario();
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void editarUsuario() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.", "Usuarios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!modoEdicion) {
            modoEdicion = true;
            txtNombre.setEditable(true);
            txtApellido.setEditable(true);
            txtUsername.setEditable(false);
            cmbRol.setEnabled(true);
            chkActivo.setEnabled(!esAdminPrincipal(usuarioSeleccionado));
            btnEditar.setText("Actualizar");
            txtNombre.requestFocusInWindow();
            return;
        }

        try {
            usuarioService.actualizar(
                    usuarioSeleccionado.getId(),
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtUsername.getText(),
                    (String) cmbRol.getSelectedItem(),
                    chkActivo.isSelected()
            );

            JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente.", "Usuarios", JOptionPane.INFORMATION_MESSAGE);
            modoEdicion = false;
            cargarUsuarios();
            nuevoUsuario();
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void desactivarUsuario() {
        if (usuarioSeleccionado == null) {
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this, "¿Desea desactivar al usuario " + usuarioSeleccionado.getUsername() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            usuarioService.desactivar(usuarioSeleccionado.getId());
            JOptionPane.showMessageDialog(this, "Usuario desactivado correctamente.", "Usuarios", JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios();
            nuevoUsuario();
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void cambiarPassword() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.", "Usuarios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPasswordField password1 = new JPasswordField();
        JPasswordField password2 = new JPasswordField();
        Object[] contenido = {"Nueva contraseña:", password1, "Confirmar contraseña:", password2};

        int opcion = JOptionPane.showConfirmDialog(this, contenido, "Cambiar contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        String nuevaPassword = new String(password1.getPassword());
        String confirmacion = new String(password2.getPassword());

        if (!nuevaPassword.equals(confirmacion)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Usuarios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            usuarioService.cambiarPassword(usuarioSeleccionado.getId(), nuevaPassword);
            JOptionPane.showMessageDialog(this, "Contraseña actualizada correctamente.", "Usuarios", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private boolean esAdminPrincipal(Usuario usuario) {
        return usuario != null && usuario.getUsername() != null && usuario.getUsername().equalsIgnoreCase("admin");
    }

    private void mostrarError(RuntimeException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}