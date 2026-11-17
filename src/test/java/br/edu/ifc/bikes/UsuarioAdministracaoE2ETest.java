package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioAdministracaoE2ETest extends UsuarioE2ETestSupport {

    @Test
    void somenteAdministradorAutenticadoPodeListarTodosOsUsuarios() {
        Usuario administrador = criarAdministrador("admin@example.com", "admin1");
        UsuarioResponseDTO cliente = cadastrarUsuario("listado@example.com", "123456");

        testClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isUnauthorized();

        String tokenCliente = autenticar("listado@example.com", "123456");
        testClient.get()
                .uri("/api/v1/usuarios")
                .header("Authorization", tokenCliente)
                .exchange()
                .expectStatus().isUnauthorized();

        String tokenAdmin = autenticar("admin@example.com", "admin1");
        List<UsuarioResponseDTO> usuarios = testClient.get()
                .uri("/api/v1/usuarios")
                .header("Authorization", tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsuarioResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(usuarios)
                .extracting(UsuarioResponseDTO::id)
                .containsExactlyInAnyOrder(administrador.getId(), cliente.id());
        assertThat(usuarios)
                .extracting(UsuarioResponseDTO::role)
                .contains("ADMIN", "CLIENTE");
    }
}
