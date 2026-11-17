package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.jwt.JwtToken;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class UsuarioE2ETestSupport {

    @LocalServerPort
    private int port;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected WebTestClient testClient;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        testClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanUp() {
        usuarioRepository.deleteAll();
    }

    protected UsuarioResponseDTO cadastrarUsuario(String username, String password) {
        UsuarioResponseDTO usuario = testClient.post()
                .uri("/api/v1/usuarios")
                .contentType(APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO(username, password))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(usuario).isNotNull();
        return usuario;
    }

    protected String autenticar(String username, String password) {
        JwtToken token = testClient.post()
                .uri("/api/v1/auth")
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of("username", username, "password", password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult()
                .getResponseBody();

        assertThat(token).isNotNull();
        assertThat(token.getToken()).isNotBlank();
        return "Bearer " + token.getToken();
    }

    protected Usuario criarAdministrador(String username, String password) {
        Usuario administrador = new Usuario();
        administrador.setUsername(username);
        administrador.setPassword(passwordEncoder.encode(password));
        administrador.setRole(Usuario.Role.ROLE_ADMIN);
        return usuarioRepository.save(administrador);
    }
}
