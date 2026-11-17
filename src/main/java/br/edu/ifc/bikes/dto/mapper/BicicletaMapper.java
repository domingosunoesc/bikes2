package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.entity.Bicicleta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BicicletaMapper {
    @Mapping(target = "id", ignore = true)
    Bicicleta toEntity(BicicletaRequestDTO request);

    BicicletaResponseDTO toResponse(Bicicleta bicicleta);
}
