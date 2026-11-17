package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class BicicletaE2ETest extends UsuarioE2ETestSupport {
    @Autowired
    private BicicletaRepository bicicletaRepository;

    @AfterEach
    void cleanUpBicicletas() {
        bicicletaRepository.deleteAll();
    }

    @Test
    void somenteAdministradorAutenticadoPodeCadastrarEConsultarPeloCodigo() {
        criarAdministrador("admin@example.com", "admin1");
        String tokenAdmin = autenticar("admin@example.com", "admin1");
        cadastrarUsuario("cliente@example.com", "123456");
        String tokenCliente = autenticar("cliente@example.com", "123456");

        testClient.post()
                .uri("/api/v1/bicicletas")
                .contentType(APPLICATION_JSON)
                .bodyValue(bicicletaRequest("BIKE-001", Bicicleta.Status.LIVRE))
                .exchange()
                .expectStatus().isUnauthorized();

        testClient.post()
                .uri("/api/v1/bicicletas")
                .header("Authorization", tokenCliente)
                .contentType(APPLICATION_JSON)
                .bodyValue(bicicletaRequest("BIKE-001", Bicicleta.Status.LIVRE))
                .exchange()
                .expectStatus().isUnauthorized();

        BicicletaResponseDTO criada = testClient.post()
                .uri("/api/v1/bicicletas")
                .header("Authorization", tokenAdmin)
                .contentType(APPLICATION_JSON)
                .bodyValue(bicicletaRequest("BIKE-001", Bicicleta.Status.LIVRE))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BicicletaResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(criada).isNotNull();
        assertThat(criada.id()).isNotNull();
        assertThat(criada.codigo()).isEqualTo("BIKE-001");
        assertThat(criada.codigo()).isNotEqualTo(criada.id().toString());
        assertThat(criada.status()).isEqualTo(Bicicleta.Status.LIVRE);

        testClient.get()
                .uri("/api/v1/bicicletas/{codigo}", criada.codigo())
                .exchange()
                .expectStatus().isUnauthorized();

        testClient.get()
                .uri("/api/v1/bicicletas/{codigo}", criada.codigo())
                .header("Authorization", tokenCliente)
                .exchange()
                .expectStatus().isUnauthorized();

        testClient.get()
                .uri("/api/v1/bicicletas/{codigo}", criada.codigo())
                .header("Authorization", tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BicicletaResponseDTO.class)
                .value(encontrada -> {
                    assertThat(encontrada.codigo()).isEqualTo("BIKE-001");
                    assertThat(encontrada.status()).isEqualTo(Bicicleta.Status.LIVRE);
                });
    }

    @Test
    void codigoDeveSerUnicoEStatusDeveSerInformado() {
        criarAdministrador("admin@example.com", "admin1");
        String tokenAdmin = autenticar("admin@example.com", "admin1");
        criarBicicleta(tokenAdmin, "BIKE-002", Bicicleta.Status.EM_MANUTENCAO);

        testClient.post()
                .uri("/api/v1/bicicletas")
                .header("Authorization", tokenAdmin)
                .contentType(APPLICATION_JSON)
                .bodyValue(bicicletaRequest("BIKE-002", Bicicleta.Status.OCUPADA))
                .exchange()
                .expectStatus().isEqualTo(409);

        testClient.post()
                .uri("/api/v1/bicicletas")
                .header("Authorization", tokenAdmin)
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of("codigo", "BIKE-003"))
                .exchange()
                .expectStatus().isEqualTo(422);

        testClient.get()
                .uri("/api/v1/bicicletas/{codigo}", "INEXISTENTE")
                .header("Authorization", tokenAdmin)
                .exchange()
                .expectStatus().isNotFound();
    }

    private BicicletaResponseDTO criarBicicleta(
            String token, String codigo, Bicicleta.Status status) {
        return testClient.post()
                .uri("/api/v1/bicicletas")
                .header("Authorization", token)
                .contentType(APPLICATION_JSON)
                .bodyValue(bicicletaRequest(codigo, status))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BicicletaResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private Map<String, Object> bicicletaRequest(String codigo, Bicicleta.Status status) {
        return Map.of("codigo", codigo, "status", status);
    }
}
