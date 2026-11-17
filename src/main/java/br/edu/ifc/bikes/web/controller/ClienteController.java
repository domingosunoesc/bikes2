package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.service.ClienteService;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Clientes", description = "Operações de cadastro e consulta de clientes.")
@RestController
@RequestMapping("api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @Operation(summary = "Cadastrar os dados do cliente",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Cliente cadastrado",
                            content = @Content(schema = @Schema(implementation = ClienteResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
                    @ApiResponse(responseCode = "403", description = "Apenas clientes podem se cadastrar"),
                    @ApiResponse(responseCode = "409", description = "Usuário ou CPF já cadastrado",
                            content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Dados inválidos",
                            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
            })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteResponseDTO> create(
            @AuthenticationPrincipal JwtUserDetails usuario,
            @Valid @RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO cliente = clienteService.create(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    @Operation(summary = "Localizar cliente pelo identificador",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente localizado",
                            content = @Content(schema = @Schema(implementation = ClienteResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
                    @ApiResponse(responseCode = "403", description = "Apenas administradores podem localizar por ID"),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
            })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.getById(id));
    }

    @Operation(summary = "Recuperar os próprios dados de cliente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Dados do cliente",
                            content = @Content(schema = @Schema(implementation = ClienteResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
                    @ApiResponse(responseCode = "403", description = "Apenas clientes podem acessar os próprios dados"),
                    @ApiResponse(responseCode = "404", description = "Cadastro de cliente não encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
            })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteResponseDTO> getCurrent(
            @AuthenticationPrincipal JwtUserDetails usuario) {
        return ResponseEntity.ok(clienteService.getByUsuarioId(usuario.getId()));
    }

    @Operation(summary = "Listar todos os clientes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de clientes"),
                    @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
                    @ApiResponse(responseCode = "403", description = "Apenas administradores podem listar")
            })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> getAll() {
        return ResponseEntity.ok(clienteService.getAll());
    }
}
