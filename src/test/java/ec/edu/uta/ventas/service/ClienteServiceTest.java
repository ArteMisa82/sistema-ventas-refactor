package ec.edu.uta.ventas.service;

import ec.edu.uta.ventas.model.Cliente;
import ec.edu.uta.ventas.repository.ClienteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest {

    private ClienteService clienteService;
    private ClienteRepositoryFalso repository;

    @BeforeEach
    void setUp() {

        repository = new ClienteRepositoryFalso();
        clienteService = new ClienteService(repository);
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    @Test
    void debeGuardarClienteValido() {

        Cliente cliente = crearClienteValido();

        clienteService.guardar(cliente);

        assertEquals(1, repository.clientes.size());
        assertEquals(
                "1801234567",
                repository.clientes.get(0).getCedula()
        );
    }

    @Test
    void noDebeGuardarClienteConCedulaDeMenosDe10Digitos() {

        Cliente cliente = crearClienteValido();
        cliente.setCedula("180123456");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.guardar(cliente)
                );

        assertEquals(
                "La cédula debe contener exactamente 10 dígitos.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeGuardarClienteConCedulaDeMasDe10Digitos() {

        Cliente cliente = crearClienteValido();
        cliente.setCedula("18012345678");

        assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.guardar(cliente)
        );
    }

    @Test
    void noDebeGuardarClienteConCedulaConLetras() {

        Cliente cliente = crearClienteValido();
        cliente.setCedula("18012ABC67");

        assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.guardar(cliente)
        );
    }

    @Test
    void noDebeGuardarClienteConTelefonoInvalido() {

        Cliente cliente = crearClienteValido();
        cliente.setTelefono("12345");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.guardar(cliente)
                );

        assertEquals(
                "El teléfono debe contener exactamente 10 dígitos.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeGuardarClienteSinNombre() {

        Cliente cliente = crearClienteValido();
        cliente.setNombre("");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.guardar(cliente)
                );

        assertEquals(
                "El nombre es obligatorio.",
                exception.getMessage()
        );
    }

    @Test
    void noDebeGuardarClienteSinApellido() {

        Cliente cliente = crearClienteValido();
        cliente.setApellido("");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.guardar(cliente)
                );

        assertEquals(
                "El apellido es obligatorio.",
                exception.getMessage()
        );
    }

    @Test
void noDebeGuardarCedulaDuplicada() {

    Cliente clienteExistente =
            crearClienteValido();

    clienteExistente.setId(1);

    repository.clientes.add(
            clienteExistente);

    Cliente nuevoCliente =
            crearClienteValido();

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> clienteService.guardar(
                            nuevoCliente)
            );

    assertEquals(
            "Ya existe un cliente con esa cédula.",
            exception.getMessage()
    );
}
    // =========================================================
    // ACTUALIZAR
    // =========================================================

    @Test
    void debeActualizarClienteValido() {

        Cliente cliente = crearClienteValido();
        cliente.setId(1);

        repository.clientes.add(cliente);

        cliente.setNombre("Carlos");

        clienteService.actualizar(cliente);

        assertEquals(
                "Carlos",
                repository.clientes.get(0).getNombre()
        );
    }

    @Test
    void noDebeActualizarClienteSinIdValido() {

        Cliente cliente = crearClienteValido();
        cliente.setId(0);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.actualizar(cliente)
                );

        assertEquals(
                "El cliente no tiene un identificador válido.",
                exception.getMessage()
        );
    }

    // =========================================================
    // DESACTIVAR
    // =========================================================

    @Test
    void debeDesactivarCliente() {

        Cliente cliente = crearClienteValido();
        cliente.setId(1);
        cliente.setActivo(true);

        repository.clientes.add(cliente);

        clienteService.desactivar(1);

        assertFalse(
                repository.clientes.get(0).isActivo()
        );
    }

    @Test
    void noDebeDesactivarConIdInvalido() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.desactivar(0)
                );

        assertEquals(
                "Identificador de cliente inválido.",
                exception.getMessage()
        );
    }

    // =========================================================
    // REACTIVAR
    // =========================================================

    @Test
    void debeReactivarCliente() {

        Cliente cliente = crearClienteValido();
        cliente.setId(1);
        cliente.setActivo(false);

        repository.clientes.add(cliente);

        clienteService.reactivar(1);

        assertTrue(
                repository.clientes.get(0).isActivo()
        );
    }

    @Test
    void noDebeReactivarConIdInvalido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.reactivar(0)
        );
    }

    // =========================================================
    // MÉTODO AUXILIAR
    // =========================================================

    private Cliente crearClienteValido() {

        Cliente cliente = new Cliente();

        cliente.setCedula("1801234567");
        cliente.setNombre("Juan");
        cliente.setSegundoNombre("Carlos");
        cliente.setApellido("Perez");
        cliente.setSegundoApellido("Lopez");
        cliente.setDireccion("Ambato");
        cliente.setTelefono("0987654321");
        cliente.setEmail("juan@gmail.com");
        cliente.setActivo(true);

        return cliente;
    }

    // =========================================================
    // REPOSITORIO FALSO
    // =========================================================

    private static class ClienteRepositoryFalso
            implements ClienteRepository {

        private final List<Cliente> clientes =
                new ArrayList<>();

        @Override
        public List<Cliente> listarActivos() {

            return clientes.stream()
                    .filter(Cliente::isActivo)
                    .toList();
        }

        @Override
        public List<Cliente> listarInactivos() {

            return clientes.stream()
                    .filter(cliente ->
                            !cliente.isActivo())
                    .toList();
        }

        @Override
        public List<Cliente> buscar(String texto) {

            String filtro =
                    texto.toLowerCase();

            return clientes.stream()
                    .filter(Cliente::isActivo)
                    .filter(cliente ->
                            contiene(
                                    cliente.getCedula(),
                                    filtro)
                            || contiene(
                                    cliente.getNombre(),
                                    filtro)
                            || contiene(
                                    cliente.getApellido(),
                                    filtro))
                    .toList();
        }

        @Override
        public Optional<Cliente> buscarPorCedula(
                String cedula) {

            return clientes.stream()
                    .filter(Cliente::isActivo)
                    .filter(cliente ->
                            cliente.getCedula()
                                    .equals(cedula))
                    .findFirst();
        }

        @Override
        public Optional<Cliente> buscarInactivoPorCedula(
                String cedula) {

            return clientes.stream()
                    .filter(cliente ->
                            !cliente.isActivo())
                    .filter(cliente ->
                            cliente.getCedula()
                                    .equals(cedula))
                    .findFirst();
        }

        @Override
        public boolean existeCedula(
                String cedula,
                int idExcluir) {

            return clientes.stream()
                    .anyMatch(cliente ->
                            cliente.getCedula()
                                    .equals(cedula)
                            && cliente.getId()
                                    != idExcluir);
        }

        @Override
        public boolean guardar(
                Cliente cliente) {

            cliente.setId(
                    clientes.size() + 1);

            cliente.setActivo(true);

            clientes.add(cliente);

            return true;
        }

        @Override
        public boolean actualizar(
                Cliente cliente) {

            for (int i = 0;
                 i < clientes.size();
                 i++) {

                if (clientes.get(i).getId()
                        == cliente.getId()) {

                    clientes.set(
                            i,
                            cliente);

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean desactivar(int id) {

            Optional<Cliente> cliente =
                    buscarPorId(id);

            if (cliente.isEmpty()) {
                return false;
            }

            cliente.get().setActivo(false);

            return true;
        }

        @Override
        public boolean reactivar(int id) {

            Optional<Cliente> cliente =
                    buscarPorId(id);

            if (cliente.isEmpty()) {
                return false;
            }

            cliente.get().setActivo(true);

            return true;
        }

        private Optional<Cliente> buscarPorId(
                int id) {

            return clientes.stream()
                    .filter(cliente ->
                            cliente.getId() == id)
                    .findFirst();
        }

        private static boolean contiene(
                String valor,
                String texto) {

            return valor != null
                    && valor.toLowerCase()
                            .contains(texto);
        }
    }
}