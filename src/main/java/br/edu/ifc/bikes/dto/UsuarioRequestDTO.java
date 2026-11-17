package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//@NotBlank testa o envio de ao menos 1 caracter
public record UsuarioRequestDTO(
        @NotBlank
        @Email(message = "O email deve ser válido")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String username,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min=6, max=6)
        String password
) { }