package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Cliente;
import ec.edu.uta.ventas.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

public class ClienteService {
    
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarActivos() {
        return clienteRepository.listarActivos();
    }

    public List<Cliente> listarInactivos() {
        return clienteRepository.listarInactivos();
    }

    public List<Cliente> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return listarActivos();
        }

        return clienteRepository.buscar(texto.trim());
    }

    public Optional<Cliente> buscarPorCedula(String cedula) {

        if (cedula == null || cedula.isBlank()) {
            return Optional.empty();
        }

        return clienteRepository.buscarPorCedula(
                cedula.trim());
    }

    public Optional<Cliente> buscarInactivoPorCedula(
            String cedula) {

        if (cedula == null ||
                cedula.isBlank()) {

            return Optional.empty();
        }

        return clienteRepository
                .buscarInactivoPorCedula(
                        cedula.trim()
                );
    }

    public void guardar(Cliente cliente) {

        validarCliente(cliente);

        if (clienteRepository.existeCedula(
                cliente.getCedula(), 0)) {

            throw new IllegalArgumentException("Ya existe un cliente con esa cédula.");
        }

        if (!clienteRepository.guardar(cliente)) {
            throw new IllegalStateException("No se pudo guardar el cliente.");
        }
    }

    public void actualizar(Cliente cliente) {

        validarCliente(cliente);

        if (cliente.getId() <= 0) {
            throw new IllegalArgumentException( "El cliente no tiene un identificador válido.");
        }

        if (clienteRepository.existeCedula(
                cliente.getCedula(),
                cliente.getId())) {

            throw new IllegalArgumentException( "Ya existe otro cliente con esa cédula.");
        }

        if (!clienteRepository.actualizar(cliente)) {
            throw new IllegalStateException( "No se pudo actualizar el cliente.");
        }
    }

    public void desactivar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Identificador de cliente inválido.");
        }

        if (!clienteRepository.desactivar(id)) {
            throw new IllegalStateException("No se pudo desactivar el cliente.");
        }
    }

    public void reactivar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Identificador de cliente inválido.");
        }

        if (!clienteRepository.reactivar(id)) {
            throw new IllegalStateException("No se pudo reactivar el cliente.");
        }
    }

    private void validarCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio.");
        }

        // Cédula
        if (cliente.getCedula() == null
                || cliente.getCedula().isBlank()) {

            throw new IllegalArgumentException(
                    "La cédula es obligatoria.");
        }

        if (!cliente.getCedula().matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "La cédula debe contener exactamente 10 dígitos.");
        }

        // Nombre
        if (cliente.getNombre() == null
                || cliente.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio.");
        }

        // Apellido
        if (cliente.getApellido() == null
                || cliente.getApellido().isBlank()) {

            throw new IllegalArgumentException(
                    "El apellido es obligatorio.");
        }

        // Teléfono
        if (cliente.getTelefono() == null
                || cliente.getTelefono().isBlank()) {

            throw new IllegalArgumentException(
                    "El teléfono es obligatorio.");
        }

        if (!cliente.getTelefono().matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "El teléfono debe contener exactamente 10 dígitos.");
        }
    }
}
