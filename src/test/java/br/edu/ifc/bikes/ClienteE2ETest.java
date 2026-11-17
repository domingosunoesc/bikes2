package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.repository.ClienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class ClienteE2ETest extends UsuarioE2ETestSupport {
    @Autowired
    private ClienteRepository clienteRepository;

    @AfterEach
    void cleanUpClientes() {
        clienteRepository.deleteAll();
    }

    @Test
    void cadastroExigeClienteAutenticadoEPermiteApenasUmCadastroPorUsuario() {
        ClienteResponseDTO cliente = criarCliente("cliente@example.com", "12345678901");

        assertThat(cliente.id()).isNotNull();
        assertThat(cliente.nomeCompleto()).isEqualTo("Cliente de Teste");
        assertThat(cliente.cpf()).isEqualTo("12345678901");
        assertThat(cliente.usuarioId()).isNotNull();

        testClient.post()
                .uri("/api/v1/clientes")
                .contentType(APPLICATION_JSON)
                .bodyValue(clienteRequest("Sem Autenticação", "98765432100"))
                .exchange()
                .expectStatus().isUnauthorized();

        criarAdministrador("admin@example.com", "admin1");
        testClient.post()
                .uri("/api/v1/clientes")
                .header("Authorization", autenticar("admin@example.com", "admin1"))
                .contentType(APPLICATION_JSON)
                .bodyValue(clienteRequest("Administrador", "98765432100"))
                .exchange()
                .expectStatus().isUnauthorized();

        testClient.post()
                .uri("/api/v1/clientes")
                .header("Authorization", autenticar("cliente@example.com", "123456"))
                .contentType(APPLICATION_JSON)
                .bodyValue(clienteRequest("Outro Nome", "98765432100"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void cpfDeveSerUnicoMesmoComPontuacaoDiferente() {
        criarCliente("primeiro@example.com", "12345678901");
        cadastrarUsuario("segundo@example.com", "123456");

        testClient.post()
                .uri("/api/v1/clientes")
                .header("Authorization", autenticar("segundo@example.com", "123456"))
                .contentType(APPLICATION_JSON)
                .bodyValue(clienteRequest("Segundo Cliente", "123.456.789-01"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void consultaPropriaEAdministrativaRespeitamAsPermissoes() {
        Usuario administrador = criarAdministrador("admin@example.com", "admin1");
        ClienteResponseDTO cliente = criarCliente("cliente@example.com", "12345678901");
        String tokenCliente = autenticar("cliente@example.com", "123456");
        String tokenAdmin = autenticar("admin@example.com", "admin1");

        testClient.get()
                .uri("/api/v1/clientes/me")
                .header("Authorization", tokenCliente)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .value(retorno -> assertThat(retorno.id()).isEqualTo(cliente.id()));

        testClient.get()
                .uri("/api/v1/clientes/{id}", cliente.id())
                .header("Authorization", tokenCliente)
                .exchange()
                .expectStatus().isUnauthorized();

        testClient.get()
                .uri("/api/v1/clientes/me")
                .header("Authorization", tokenAdmin)
                .exchange()
                .expectStatus().isUnauthorized();

        testClient.get()
                .uri("/api/v1/clientes/{id}", cliente.id())
                .header("Authorization", tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .value(retorno -> assertThat(retorno.id()).isEqualTo(cliente.id()));

        testClient.get()
                .uri("/api/v1/clientes")
                .header("Authorization", tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClienteResponseDTO.class)
                .hasSize(1);

        assertThat(administrador.getId()).isNotNull();
    }

    private ClienteResponseDTO criarCliente(String username, String cpf) {
        cadastrarUsuario(username, "123456");
        return testClient.post()
                .uri("/api/v1/clientes")
                .header("Authorization", autenticar(username, "123456"))
                .contentType(APPLICATION_JSON)
                .bodyValue(clienteRequest("Cliente de Teste", cpf))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClienteResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private Object clienteRequest(String nomeCompleto, String cpf) {
        return java.util.Map.of("nomeCompleto", nomeCompleto, "cpf", cpf);
    }
}
