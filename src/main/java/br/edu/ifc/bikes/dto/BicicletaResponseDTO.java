package br.edu.ifc.bikes.dto;

import br.edu.ifc.bikes.entity.Bicicleta;

public record BicicletaResponseDTO(
        Long id,
        String codigo,
        Bicicleta.Status status
) {
}
