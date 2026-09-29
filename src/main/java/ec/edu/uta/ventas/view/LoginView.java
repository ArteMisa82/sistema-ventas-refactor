package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.model.Usuario;
import ec.edu.uta.ventas.service.AutenticacionService;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JFrame {
    private final AutenticacionService autenticacionService;
    private final Runnable alIniciarSesion;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;

    public LoginView(AutenticacionService autenticacionService, Runnable alIniciarSesion) {
        this.autenticacionService = autenticacionService;
        this.alIniciarSesion = alIniciarSesion;

        configurarVentana();
        crearComponentes();
        configurarEventos();
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void configurarVentana() {
        setTitle("Sistema de Ventas - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.FONDO);
    }

    private void crearComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 20));
        panelPrincipal.setBackground(EstiloUI.BLANCO);
        panelPrincipal.setBorder(new EmptyBorder(35, 55, 35, 55));

        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(EstiloUI.BLANCO);

        JLabel lblTitulo = new JLabel("INICIAR SESIÓN", SwingConstants.CENTER);
        EstiloUI.titulo(lblTitulo);

        JLabel lblDescripcion = new JLabel("Sistema de Venta y Facturación", SwingConstants.CENTER);
        lblDescripcion.setForeground(EstiloUI.AZUL_SECUNDARIO);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 5));
        textos.setBackground(EstiloUI.BLANCO);
        textos.add(lblTitulo);
        textos.add(lblDescripcion);

        panelEncabezado.add(textos, BorderLayout.CENTER);
        panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(EstiloUI.BLANCO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JLabel lblUsuario = new JLabel("USUARIO:");
        JLabel lblPassword = new JLabel("CONTRASEÑA:");

        EstiloUI.subtitulo(lblUsuario);
        EstiloUI.subtitulo(lblPassword);

        txtUsuario = new JTextField(20);
        txtPassword = new JPasswordField(20);

        EstiloUI.campo(txtUsuario);
        EstiloUI.campoPassword(txtPassword);

        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0, 5, 0);
        panelFormulario.add(lblUsuario, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        panelFormulario.add(txtUsuario, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 5, 0);
        panelFormulario.add(lblPassword, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 22, 0);
        panelFormulario.add(txtPassword, gbc);

        btnIngresar = new JButton("INGRESAR");
        EstiloUI.botonPrincipal(btnIngresar);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 20, 0, 20);
        panelFormulario.add(btnIngresar, gbc);

        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        add(panelPrincipal);
    }

    private void configurarEventos() {
        btnIngresar.addActionListener(e -> iniciarSesion());
        txtUsuario.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(e -> iniciarSesion());
    }

    private void iniciarSesion() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        try {
            Usuario usuario = autenticacionService.iniciarSesion(username, password);
            abrirMenuSegunRol(usuario);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Inicio de sesión", JOptionPane.WARNING_MESSAGE);
            limpiarPassword();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo iniciar sesión.\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            limpiarPassword();
        }
    }

    private void abrirMenuSegunRol(Usuario usuario) {
        if (!usuario.isAdmin() && !usuario.isCajero()) {
            JOptionPane.showMessageDialog(this, "Rol no reconocido: " + usuario.getRol(), "Error", JOptionPane.ERROR_MESSAGE);
            autenticacionService.cerrarSesion();
            return;
        }

        dispose();
        alIniciarSesion.run();
    }

    private void limpiarPassword() {
        txtPassword.setText("");
        txtPassword.requestFocusInWindow();
    }
}