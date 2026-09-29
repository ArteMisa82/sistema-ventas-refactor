package ec.edu.uta.ventas.view.style;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class EstiloUI {

    /*
     * =========================
     * COLORES
     * =========================
     */

    public static final Color AZUL_MARINO =
            new Color(15, 45, 75);

    public static final Color AZUL_SECUNDARIO =
            new Color(30, 75, 115);

    public static final Color AZUL_CLARO =
            new Color(220, 232, 243);

    public static final Color BLANCO =
            Color.WHITE;

    public static final Color FONDO =
            new Color(248, 249, 251);

    public static final Color TEXTO =
            new Color(35, 35, 35);

    public static final Color BORDE =
            new Color(190, 200, 210);

    public static final Color ROJO =
            new Color(190, 45, 45);

    private EstiloUI() {
        /*
         * Evita instanciar esta clase.
         */
    }

    /*
     * =========================
     * PANELES
     * =========================
     */

    public static void panelBlanco(
            JPanel panel) {

        panel.setBackground(BLANCO);
    }

    public static void fondo(
            Container container) {

        container.setBackground(FONDO);
    }

    /*
     * =========================
     * TÍTULOS
     * =========================
     */

    public static void titulo(
            JLabel label) {

        label.setForeground(AZUL_MARINO);

        label.setFont(
                label.getFont().deriveFont(
                        Font.BOLD,
                        20f
                )
        );
    }

    public static void subtitulo(
            JLabel label) {

        label.setForeground(AZUL_MARINO);

        label.setFont(
                label.getFont().deriveFont(
                        Font.BOLD,
                        13f
                )
        );
    }

    /*
     * =========================
     * BOTONES
     * =========================
     */

    public static void botonPrincipal(
            JButton boton) {

        boton.setBackground(AZUL_MARINO);
        boton.setForeground(BLANCO);

        boton.setFont(
                boton.getFont().deriveFont(
                        Font.BOLD
                )
        );

        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        boton.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        16,
                        8,
                        16
                )
        );
    }

    public static void botonSecundario(
            JButton boton) {

        boton.setBackground(BLANCO);
        boton.setForeground(AZUL_MARINO);

        boton.setFont(
                boton.getFont().deriveFont(
                        Font.BOLD
                )
        );

        boton.setFocusPainted(false);

        boton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        boton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                AZUL_MARINO
                        ),
                        BorderFactory.createEmptyBorder(
                                7,
                                15,
                                7,
                                15
                        )
                )
        );
    }

    /*
     * =========================
     * CAMPOS
     * =========================
     */

    public static void campo(
            JTextField campo) {

        campo.setBackground(BLANCO);
        campo.setForeground(TEXTO);
        campo.setCaretColor(AZUL_MARINO);

        campo.setBorder(
                crearBordeCampo()
        );
    }

    public static void campoPassword(
            JPasswordField campo) {

        campo.setBackground(BLANCO);
        campo.setForeground(TEXTO);
        campo.setCaretColor(AZUL_MARINO);

        campo.setBorder(
                crearBordeCampo()
        );
    }

    private static Border crearBordeCampo() {

        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        BORDE
                ),
                BorderFactory.createEmptyBorder(
                        5,
                        7,
                        5,
                        7
                )
        );
    }

    /*
     * =========================
     * TABLAS
     * =========================
     */

    public static void tabla(
            JTable tabla) {

        tabla.setBackground(BLANCO);
        tabla.setForeground(TEXTO);

        tabla.setSelectionBackground(
                AZUL_CLARO
        );

        tabla.setSelectionForeground(
                TEXTO
        );

        tabla.setRowHeight(26);

        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);

        tabla.setGridColor(
                new Color(
                        225,
                        230,
                        235
                )
        );

        tabla.setFillsViewportHeight(true);

        JTableHeader header =
                tabla.getTableHeader();

        header.setBackground(
                AZUL_MARINO
        );

        header.setForeground(
                BLANCO
        );

        header.setFont(
                header.getFont().deriveFont(
                        Font.BOLD
                )
        );

        header.setPreferredSize(
                new Dimension(
                        header.getPreferredSize().width,
                        30
                )
        );
    }

    /*
     * =========================
     * SCROLL
     * =========================
     */

    public static void scroll(JScrollPane scrollPane) {

        scrollPane.getViewport()
                .setBackground(BLANCO);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        BORDE
                )
        );
    }
}