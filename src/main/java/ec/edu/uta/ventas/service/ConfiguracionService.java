package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Configuracion;
import ec.edu.uta.ventas.repository.ConfiguracionRepository;

import java.math.BigDecimal;
import java.util.List;

public class ConfiguracionService {

    public static final String CLAVE_IVA = "IVA";
    public static final String CLAVE_STOCK_MINIMO = "STOCK_MINIMO";
    public static final String CLAVE_EMPRESA_NOMBRE = "EMPRESA_NOMBRE";
    public static final String CLAVE_EMPRESA_RUC = "EMPRESA_RUC";
    public static final String CLAVE_EMPRESA_DIR = "EMPRESA_DIR";

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(
            ConfiguracionRepository configuracionRepository) {

        this.configuracionRepository =
                configuracionRepository;
    }

    /*
     * =========================
     * CONSULTAS
     * =========================
     */

    public List<Configuracion> listar() {

        return configuracionRepository.listar();
    }

    public Configuracion buscarPorClave(
            String clave) {

        validarClave(clave);

        return configuracionRepository
                .buscarPorClave(clave)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe la configuración: "
                                        + clave
                        )
                );
    }

    public String obtenerValor(
            String clave) {

        return buscarPorClave(clave)
                .getValor();
    }

    /*
     * =========================
     * IVA
     * =========================
     */

    public BigDecimal obtenerIva() {

        String valor =
                obtenerValor(CLAVE_IVA);

        try {

            return new BigDecimal(valor);

        } catch (NumberFormatException e) {

            throw new IllegalStateException(
                    "El IVA configurado no es válido."
            );
        }
    }

    public boolean actualizarIva(
            BigDecimal iva) {

        if (iva == null) {

            throw new IllegalArgumentException(
                    "El IVA es obligatorio."
            );
        }

        if (iva.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El IVA no puede ser negativo."
            );
        }

        if (iva.compareTo(
                new BigDecimal("100")) > 0) {

            throw new IllegalArgumentException(
                    "El IVA no puede ser mayor a 100."
            );
        }

        return actualizarValor(
                CLAVE_IVA,
                iva.stripTrailingZeros()
                        .toPlainString()
        );
    }

    /*
     * =========================
     * STOCK MÍNIMO
     * =========================
     */

    public int obtenerStockMinimo() {

        String valor =
                obtenerValor(
                        CLAVE_STOCK_MINIMO
                );

        try {

            return Integer.parseInt(valor);

        } catch (NumberFormatException e) {

            throw new IllegalStateException(
                    "El stock mínimo configurado "
                            + "no es válido."
            );
        }
    }

    public boolean actualizarStockMinimo(
            int stockMinimo) {

        if (stockMinimo < 0) {

            throw new IllegalArgumentException(
                    "El stock mínimo "
                            + "no puede ser negativo."
            );
        }

        return actualizarValor(
                CLAVE_STOCK_MINIMO,
                String.valueOf(stockMinimo)
        );
    }

    /*
     * =========================
     * DATOS DE EMPRESA
     * =========================
     */

    public String obtenerNombreEmpresa() {

        return obtenerValor(
                CLAVE_EMPRESA_NOMBRE
        );
    }

    public String obtenerRucEmpresa() {

        return obtenerValor(
                CLAVE_EMPRESA_RUC
        );
    }

    public String obtenerDireccionEmpresa() {

        return obtenerValor(
                CLAVE_EMPRESA_DIR
        );
    }

    public boolean actualizarNombreEmpresa(
            String nombre) {

        validarTextoObligatorio(
                nombre,
                "El nombre de la empresa "
                        + "es obligatorio."
        );

        return actualizarValor(
                CLAVE_EMPRESA_NOMBRE,
                nombre.trim()
        );
    }

    public boolean actualizarRucEmpresa(
            String ruc) {

        validarTextoObligatorio(
                ruc,
                "El RUC de la empresa "
                        + "es obligatorio."
        );

        String rucLimpio =
                ruc.trim();

        if (!rucLimpio.matches("\\d{13}")) {

            throw new IllegalArgumentException(
                    "El RUC debe contener "
                            + "13 dígitos."
            );
        }

        return actualizarValor(
                CLAVE_EMPRESA_RUC,
                rucLimpio
        );
    }

    public boolean actualizarDireccionEmpresa(
            String direccion) {

        validarTextoObligatorio(
                direccion,
                "La dirección de la empresa "
                        + "es obligatoria."
        );

        return actualizarValor(
                CLAVE_EMPRESA_DIR,
                direccion.trim()
        );
    }

    /*
     * =========================
     * ACTUALIZACIÓN GENERAL
     * =========================
     */

    private boolean actualizarValor(
            String clave,
            String valor) {

        validarClave(clave);

        boolean actualizado =
                configuracionRepository.actualizar(
                        clave,
                        valor
                );

        if (!actualizado) {

            throw new IllegalArgumentException(
                    "No existe la configuración: "
                            + clave
            );
        }

        return true;
    }

    /*
     * =========================
     * VALIDACIONES
     * =========================
     */

    private void validarClave(
            String clave) {

        if (clave == null
                || clave.isBlank()) {

            throw new IllegalArgumentException(
                    "La clave de configuración "
                            + "es obligatoria."
            );
        }
    }

    private void validarTextoObligatorio(
            String valor,
            String mensaje) {

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }
}