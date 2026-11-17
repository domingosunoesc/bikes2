package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.service.BicicletaService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bicicletas", description = "Operações administrativas de cadastro e consulta de bicicletas.")
@RestController
@RequestMapping("api/v1/bicicletas")
@RequiredArgsConstructor
public class BicicletaController {
    private final BicicletaService bicicletaService;

    @Operation(summary = "Cadastrar bicicleta",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Bicicleta cadastrada",
                            content = @Content(schema = @Schema(implementation = BicicletaResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
                    @ApiResponse(responseCode = "409", description = "Código já cadastrado",
                            content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Dados inválidos",
                            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
            })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BicicletaResponseDTO> create(
            @Valid @RequestBody BicicletaRequestDTO request) {
        BicicletaResponseDTO bicicleta = bicicletaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bicicleta);
    }

    @Operation(summary = "Localizar bicicleta pelo código",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bicicleta localizada",
                            content = @Content(schema = @Schema(implementation = BicicletaResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
                    @ApiResponse(responseCode = "404", description = "Bicicleta não encontrada",
                            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
            })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BicicletaResponseDTO> getByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(bicicletaService.getByCodigo(codigo));
    }
}
