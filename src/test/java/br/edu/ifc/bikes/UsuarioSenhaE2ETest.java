package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class UsuarioSenhaE2ETest extends UsuarioE2ETestSupport {

    @Test
    void usuarioAlteraSenhaEAsNovasCredenciaisPersistem() {
        UsuarioResponseDTO usuario = cadastrarUsuario("senha@example.com", "123456");
        String token = autenticar("senha@example.com", "123456");

        testClient.patch()
                .uri("/api/v1/usuarios/{id}", usuario.id())
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of(
                        "novaSenha", "654321",
                        "confirmaSenha", "654321",
                        "senhaAtual", "123456"))
                .exchange()
                .expectStatus().isNoContent();

        testClient.post()
                .uri("/api/v1/auth")
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of("username", "senha@example.com", "password", "123456"))
                .exchange()
                .expectStatus().isBadRequest();

        String novoToken = autenticar("senha@example.com", "654321");
        UsuarioResponseDTO persistido = testClient.get()
                .uri("/api/v1/usuarios/{id}", usuario.id())
                .header("Authorization", novoToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(persistido).isNotNull();
        assertThat(persistido.id()).isEqualTo(usuario.id());
    }

    @Test
    void alteracaoRejeitaSenhaCurtaOuConfirmacaoDiferente() {
        UsuarioResponseDTO usuario = cadastrarUsuario("validacao@example.com", "123456");
        String token = autenticar("validacao@example.com", "123456");

        testClient.patch()
                .uri("/api/v1/usuarios/{id}", usuario.id())
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of(
                        "novaSenha", "12345",
                        "confirmaSenha", "12345",
                        "senhaAtual", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422);

        testClient.patch()
                .uri("/api/v1/usuarios/{id}", usuario.id())
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of(
                        "novaSenha", "654321",
                        "confirmaSenha", "123456",
                        "senhaAtual", "123456"))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
