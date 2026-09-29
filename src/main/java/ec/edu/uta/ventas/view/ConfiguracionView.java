package ec.edu.uta.ventas.view;

import ec.edu.uta.ventas.service.ConfiguracionService;
import ec.edu.uta.ventas.view.style.EstiloUI;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ConfiguracionView extends JInternalFrame {
    private final ConfiguracionService configuracionService;

    private JTextField txtIva;
    private JTextField txtStockMinimo;
    private JTextField txtNombreEmpresa;
    private JTextField txtRucEmpresa;
    private JTextField txtDireccionEmpresa;
    private JButton btnGuardar;

    public ConfiguracionView(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;

        configurarVentana();
        crearComponentes();
        cargarConfiguracion();
    }

    private void configurarVentana() {
        setTitle("Configuración del Sistema");
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        setSize(650, 450);
    }

    private void crearComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(EstiloUI.FONDO);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("CONFIGURACIÓN DEL SISTEMA", SwingConstants.LEFT);
        EstiloUI.titulo(lblTitulo);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(EstiloUI.BLANCO);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Parámetros del sistema"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtIva = new JTextField(20);
        txtStockMinimo = new JTextField(20);
        txtNombreEmpresa = new JTextField(20);
        txtRucEmpresa = new JTextField(20);
        txtDireccionEmpresa = new JTextField(20);

        EstiloUI.campo(txtIva);
        EstiloUI.campo(txtStockMinimo);
        EstiloUI.campo(txtNombreEmpresa);
        EstiloUI.campo(txtRucEmpresa);
        EstiloUI.campo(txtDireccionEmpresa);

        int fila = 0;
        agregarCampo(panelFormulario, gbc, fila++, "IVA (%):", txtIva);
        agregarCampo(panelFormulario, gbc, fila++, "Stock mínimo:", txtStockMinimo);
        agregarCampo(panelFormulario, gbc, fila++, "Nombre de empresa:", txtNombreEmpresa);
        agregarCampo(panelFormulario, gbc, fila++, "RUC:", txtRucEmpresa);
        agregarCampo(panelFormulario, gbc, fila, "Dirección:", txtDireccionEmpresa);

        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(EstiloUI.FONDO);

        btnGuardar = new JButton("Guardar cambios");
        EstiloUI.botonPrincipal(btnGuardar);
        btnGuardar.addActionListener(e -> guardarConfiguracion());

        panelBotones.add(btnGuardar);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        setContentPane(panelPrincipal);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String texto, JTextField campo) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.0;

        JLabel label = new JLabel(texto);
        label.setForeground(EstiloUI.TEXTO);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(campo, gbc);
    }

    private void cargarConfiguracion() {
        try {
            txtIva.setText(configuracionService.obtenerIva().toPlainString());
            txtStockMinimo.setText(String.valueOf(configuracionService.obtenerStockMinimo()));
            txtNombreEmpresa.setText(configuracionService.obtenerNombreEmpresa());
            txtRucEmpresa.setText(configuracionService.obtenerRucEmpresa());
            txtDireccionEmpresa.setText(configuracionService.obtenerDireccionEmpresa());
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarConfiguracion() {
        try {
            BigDecimal iva = new BigDecimal(txtIva.getText().trim());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
            String nombreEmpresa = txtNombreEmpresa.getText().trim();
            String ruc = txtRucEmpresa.getText().trim();
            String direccion = txtDireccionEmpresa.getText().trim();

            configuracionService.actualizarIva(iva);
            configuracionService.actualizarStockMinimo(stockMinimo);
            configuracionService.actualizarNombreEmpresa(nombreEmpresa);
            configuracionService.actualizarRucEmpresa(ruc);
            configuracionService.actualizarDireccionEmpresa(direccion);

            JOptionPane.showMessageDialog(this, "Configuración actualizada correctamente.", "Configuración", JOptionPane.INFORMATION_MESSAGE);
            cargarConfiguracion();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El IVA y el stock mínimo deben contener valores numéricos válidos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar la configuración.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}