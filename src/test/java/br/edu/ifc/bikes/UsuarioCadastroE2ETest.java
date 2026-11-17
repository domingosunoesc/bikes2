package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class UsuarioCadastroE2ETest extends UsuarioE2ETestSupport {

    @Test
    void cadastroPublicoCriaClienteUnicoEPermiteBuscaPorIdComAutenticacao() {
        UsuarioResponseDTO criado = cadastrarUsuario("cliente@example.com", "123456");

        assertThat(criado.id()).isNotNull();
        assertThat(criado.username()).isEqualTo("cliente@example.com");
        assertThat(criado.role()).isEqualTo("CLIENTE");

        testClient.get()
                .uri("/api/v1/usuarios/{id}", criado.id())
                .exchange()
                .expectStatus().isUnauthorized();

        String token = autenticar("cliente@example.com", "123456");
        UsuarioResponseDTO encontrado = testClient.get()
                .uri("/api/v1/usuarios/{id}", criado.id())
                .header("Authorization", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.id()).isEqualTo(criado.id());
        assertThat(encontrado.username()).isEqualTo(criado.username());

        testClient.post()
                .uri("/api/v1/usuarios")
                .contentType(APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("cliente@example.com", "654321"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void cadastroRejeitaEmailInvalidoESenhaForaDoTamanhoExato() {
        testClient.post()
                .uri("/api/v1/usuarios")
                .contentType(APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("email-invalido", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422);

        testClient.post()
                .uri("/api/v1/usuarios")
                .contentType(APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("curta@example.com", "12345"))
                .exchange()
                .expectStatus().isEqualTo(422);

        testClient.post()
                .uri("/api/v1/usuarios")
                .contentType(APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("longa@example.com", "1234567"))
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void cadastroPublicoNaoPermiteEscolherPerfilAdministrador() {
        UsuarioResponseDTO criado = testClient.post()
                .uri("/api/v1/usuarios")
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", "perfil@example.com",
                        "password", "123456",
                        "role", "ROLE_ADMIN"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(criado).isNotNull();
        assertThat(criado.role()).isEqualTo("CLIENTE");
    }
}
