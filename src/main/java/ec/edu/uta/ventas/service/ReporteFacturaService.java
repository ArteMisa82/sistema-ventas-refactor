package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.database.ConnectionProvider;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReporteFacturaService {

    private final ConnectionProvider connectionProvider;
    private final ConfiguracionService configuracionService;

    public ReporteFacturaService(
            ConnectionProvider connectionProvider,
            ConfiguracionService configuracionService) {

        this.connectionProvider = connectionProvider;
        this.configuracionService = configuracionService;
    }

    public void mostrarFactura(int idVenta) {

        if (idVenta <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la venta no es válido."
            );
        }

        try (
                InputStream reporteStream =
                        getClass()
                                .getResourceAsStream(
                                        "/Reportes/factura.jrxml"
                                );

                InputStream logoStream =
                        getClass()
                                .getResourceAsStream(
                                        "/Reportes/logos.png"
                                );

                Connection connection =
                        connectionProvider.getConnection()
        ) {

            if (reporteStream == null) {
                throw new IllegalStateException(
                        "No se encontró el reporte factura.jrxml."
                );
            }

            if (logoStream == null) {
                throw new IllegalStateException(
                        "No se encontró el logo de la factura."
                );
            }

            Map<String, Object> parametros =
                    new HashMap<>();

            parametros.put(
                    "idVenta",
                    idVenta
            );

            parametros.put(
                    "logo",
                    logoStream
            );

            parametros.put(
                    "empresaNombre",
                    configuracionService
                            .obtenerNombreEmpresa()
            );

            parametros.put(
                    "empresaRuc",
                    configuracionService
                            .obtenerRucEmpresa()
            );

            parametros.put(
                    "empresaDireccion",
                    configuracionService
                            .obtenerDireccionEmpresa()
            );

            parametros.put(
                    "iva",
                    configuracionService
                            .obtenerIva()
            );

            var jasperReport =
                    JasperCompileManager
                            .compileReport(
                                    reporteStream
                            );

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport(
                            jasperReport,
                            parametros,
                            connection
                    );

            JasperViewer.viewReport(
                    jasperPrint,
                    false
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo generar la factura: "
                            + e.getMessage(),
                    e
            );
        }
    }
}